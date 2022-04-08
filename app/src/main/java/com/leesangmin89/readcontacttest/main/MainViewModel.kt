package com.leesangmin89.readcontacttest.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.leesangmin89.readcontacttest.data.dao.*
import com.leesangmin89.readcontacttest.data.entity.ContactInfo
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.entity.Tendency
import com.leesangmin89.readcontacttest.util.CombinedChartData
import com.leesangmin89.readcontacttest.util.convertLongToMonthLong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataInfo: ContactInfoDao,
    private val dataBase: ContactDao,
    private val dataGroup: GroupListDao,
    private val dataCall: CallLogDao,
    private val dataTen: TendencyDao,
    private val dataReco: RecommendationDao,
    application: Application
) : AndroidViewModel(application) {

    private val _countNumbers = MutableLiveData<CountNumbers>()
    val countNumbers: LiveData<CountNumbers> = _countNumbers

    private val _mainGroupChartData = MutableLiveData<List<MainGroupChartData>>()
    val mainGroupChartData: LiveData<List<MainGroupChartData>> = _mainGroupChartData

    private val _makeRecommendationInfoEvent = MutableLiveData<Boolean>()
    val makeRecommendationInfoEvent: LiveData<Boolean> = _makeRecommendationInfoEvent

    private val _progressBarEventFinished = MutableLiveData<Boolean>()
    val progressBarEventFinished: LiveData<Boolean> = _progressBarEventFinished

    private var emptyCheck: Boolean = false

    fun emptyCheck() {
        viewModelScope.launch {
            val checkList = dataBase.getAllContactBaseList()
            if (checkList == emptyList<ContactBase>()) {
                emptyCheck = true
            }
        }
    }

    // 앱 최초 빌드 시, 연락처를 불러오는 함수
    @SuppressLint("Range")
    fun appBuildLoadContact(activity: Activity) {
        viewModelScope.launch {
            val checkList = dataBase.getAllContactBaseList()
            if (checkList == emptyList<ContactBase>()) {
                // 1. ContactBase 가 empty 이면 ContactBase DB 생성
                val contacts = activity.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
                )

                while (contacts!!.moveToNext()) {
                    val name =
                        contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    // 번호 수집 시, - 일괄 제거하여 수집한다.
                    val number =
                        contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replace("-", "")
                    val photoUri =
                        contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
                    val contactList = ContactBase(name, number, "", null, 0)
                    if (photoUri != null) {
                        if (Build.VERSION.SDK_INT < 28) {
                            contactList.image = MediaStore.Images.Media.getBitmap(
                                activity.contentResolver,
                                Uri.parse(photoUri)
                            )
                        } else {
                            val source = ImageDecoder.createSource(
                                activity.contentResolver,
                                Uri.parse(photoUri)
                            )
                            val bitmap = ImageDecoder.decodeBitmap(source)
                            contactList.image = bitmap
                        }
                    }
                    insertContactBase(contactList)
                }
                contacts.close()

                // 연락처를 다시 가져올 때마다, 연락처-그룹 동기화 하는 코드
                updateGroupNameInContactBase()
            }
            // 2. 전화 통계, 통화기록 DB 생성
            getPhoneInfo(activity)
        }
    }

    fun insertContactBase(contact: ContactBase) {
        viewModelScope.launch {
            dataBase.insert(contact)
        }
    }

    // 연락처를 다시 가져올 때마다, 연락처-그룹 동기화 하는 코드
    private fun updateGroupNameInContactBase() {
        viewModelScope.launch {
            for (contact in dataBase.getAllContactBaseList()) {
                for (groupList in dataGroup.getAllDataFromGroupList()) {
                    if (contact.number == groupList.number) {
                        val updateList = ContactBase(
                            contact.name,
                            contact.number,
                            groupList.group,
                            contact.image,
                            contact.id
                        )
                        dataBase.update(updateList)
                    }
                }
            }
        }
    }

    // 2. 전화 통계, 통화기록 DB 생성
    @SuppressLint("Range")
    fun getPhoneInfo(activity: Activity) {
        viewModelScope.launch {

            // 전화 로그 가져오는 uri
            val callLogUri = CallLog.Calls.CONTENT_URI

            val contacts = activity.contentResolver.query(
                callLogUri,
                null,
                null,
                null,
                null
            )

            // 반복 작업 구간
            while (contacts!!.moveToNext()) {
                var name =
                    contacts.getString(contacts.getColumnIndex(CallLog.Calls.CACHED_NAME))
                val number =
                    contacts.getString(contacts.getColumnIndex(CallLog.Calls.NUMBER))
                val duration =
                    contacts.getString(contacts.getColumnIndex(CallLog.Calls.DURATION))
                val date =
                    contacts.getString(contacts.getColumnIndex(CallLog.Calls.DATE))
                val callType =
                    contacts.getString(contacts.getColumnIndex(CallLog.Calls.TYPE))

                if (name == null) {
                    name = number
                }

                // 필요시만 갱신 ★
                // CallLogData 통화기록 데이터 갱신
                // 없는 기록만 insert, Tendency DB update
                val check = dataCall.confirmAndInsert(number, date, duration)
                if (check == null) {
                    // 기존에 없는 CallLog 가 발생하면 insert ★
                    val callLogListChild = CallLogData(name, number, date, duration, callType)
                    dataCall.insert(callLogListChild)

                    // 추가로 Tendency AllCall 관련 데이터도 업데이트,
                    val preTendency = dataTen.getRecentTendency()
                    if (preTendency != null) {
                        // 기존에 없는 CallLog 가 발생하고,
                        // Tendency DB 가 기존에 존재할 때, 업데이트 함
                        var allCallIncoming = 0
                        var allCallOutgoing = 0
                        var allCallMissed = 0
                        var allCallCount = 0
                        var allCallDuration = 0
                        var allCallPartnerList = mutableListOf<String>()

                        allCallCount += 1
                        allCallDuration += duration.toInt()
                        allCallPartnerList.add(number)

                        // 통화 유형
                        when (callType.toInt()) {
                            1 -> allCallIncoming += 1
                            2 -> allCallOutgoing += 1
                            3 -> allCallMissed += 1
                            else -> {}
                        }

                        allCallIncoming += preTendency.allCallIncoming.toInt()
                        allCallOutgoing += preTendency.allCallOutgoing.toInt()
                        allCallMissed += preTendency.allCallMissed.toInt()
                        allCallCount += preTendency.allCallCount.toInt()
                        allCallDuration += preTendency.allCallDuration.toInt()
                        allCallPartnerList =
                            (preTendency.allCallPartnerList + allCallPartnerList) as MutableList<String>

                        val updateList = Tendency(
                            allCallIncoming.toString(),
                            allCallOutgoing.toString(),
                            allCallMissed.toString(),
                            allCallCount.toString(),
                            allCallDuration.toString(),
                            allCallPartnerList.distinct(),
                            preTendency.groupListCallIncoming,
                            preTendency.groupListCallOutgoing,
                            preTendency.groupListCallMissed,
                            preTendency.groupListCallCount,
                            preTendency.groupListCallDuration,
                            preTendency.groupListCallPartnerList,
                            preTendency.recommendationCallIncoming,
                            preTendency.recommendationCallOutgoing,
                            preTendency.recommendationCallMissed,
                            preTendency.recommendationCallCount,
                            preTendency.recommendationCallDuration,
                            preTendency.recommendationCallPartnerList,
                            preTendency.id
                        )
                        dataTen.update(updateList)
                    }
                }

            }
            contacts.close()
            // 콜로그 데이터를 다 가져온 뒤, 확실히 넘어가야 함!!
            makeRecommendationInfoEvent()
        }
    }

    // MainSubFragment Used
    // 월별 통화시간과 통화횟수를 결산하는 함수
    // 매번 갱신
    @SuppressLint("Range")
    fun contactInfoUpdate(activity: Activity): List<CombinedChartData> {
        // 전화 로그 가져오는 uri
        val callLogUri = CallLog.Calls.CONTENT_URI

        var month = 0L
        var callCountNum = 0
        var callDuration = 0L

        val list = mutableListOf<CombinedChartData>()

        val contacts = activity.contentResolver.query(
            callLogUri,
            null,
            null,
            null,
            null
        )

        // 최근것부터 불러옴
        while (contacts!!.moveToNext()) {
            // 통화시간 : 초
            val duration =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.DURATION)).toLong()
            // 통화일자 : 밀리초
            val date =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.DATE)).toLong()

            val cycleMonth = convertLongToMonthLong(date)

            if (month != cycleMonth) {
                list.add(CombinedChartData(month, callCountNum, callDuration))
                month = cycleMonth
            } else {
                callCountNum += 1
                callDuration += duration
            }
        }
        contacts.close()

        // 0,0,0 요소 삭제
        list.removeAt(0)

        // 최근 일자부터 재정렬
//        list.sortByDescending { it.month }

        list.sortBy { it.month }

        // 12개 제한
//        list.take(12)
        // 다시 예전부터 정렬
//        list.reverse()

        //반환
        return list
    }


    private fun makeRecommendationInfoEvent() {
        _makeRecommendationInfoEvent.value = true
    }

    fun makeRecommendationInfoEventDone() {
        _makeRecommendationInfoEvent.value = false
    }

    // CountNumber 를 추출하는 함수
    fun syncCountNumbers() {
        viewModelScope.launch {
            var contactNumber = 0
            var groupListNumber = 0
            var recoListNumber = 0
            var recoActivatedNumber = 0

            contactNumber = dataBase.getNumbersContactBaseList().count()

            groupListNumber = dataGroup.getNumberFromGroupList().count()

            recoListNumber = dataReco.getAllNumbers().count()

            recoActivatedNumber = dataReco.getNumberDataByRecommended().count()

            _countNumbers.value =
                CountNumbers(contactNumber, groupListNumber, recoListNumber, recoActivatedNumber)
        }
    }

    fun getGroupNameListDataLive(): LiveData<List<String>> {
        return dataGroup.getGroupNameFlow().asLiveData()
    }

    // MainFragment 의 GroupChart 데이터 생성을 위한 함수
    fun getGroupChartData(groupList: List<String>) {
        // 그룹 이름이 있는 것들을 리스트 형태로 모은 변수(그룹 생성 순으로 불러온다)
        val data: List<String> = groupList
        // GroupData 를 넣을 빈 리스트 변수
        val groupData = mutableListOf<MainGroupChartData>()

        // 맵 : 키-그룹명, 값-사람수
        val groupDataMap = mutableMapOf<String, Int>()


        // 그룹명을 리스트 형태로 모아서, 그룹당 사람 수를 출력하는 코드
        // 만약 data 가 empty 상태라면
        if (data == emptyList<String>()) {
            _mainGroupChartData.postValue(groupData)
        } else {
            // data 가 empty 가 아니라면
            // data 를 순회하면서
            for (groupName in data) {
                if (groupDataMap.contains(groupName)) {
                    val preValue = groupDataMap[groupName]
                    if (preValue != null) {
                        // 기존 값이 있으면 1을 더하고
                        groupDataMap[groupName] = preValue + 1
                    }
                } else {
                    // 없으면 1로 시작한다.
                    groupDataMap[groupName] = 1
                }
            }

            // groupDataMap 을 순회하면서 name : 그룹명, numbers : 사람수
            for ((name, numbers) in groupDataMap) {
                val list = MainGroupChartData(name, numbers.toFloat())
                groupData.add(list)
            }

            // 사람수가 많은 그룹부터 정렬하여 최종 출력한다.
            val result = groupData.sortedByDescending { it.groupNumber.toInt() }
            _mainGroupChartData.postValue(result)
        }
    }
}

@Parcelize
data class CountNumbers(
    var contactNumber: Int,
    var groupNumber: Int,
    var recoNumber: Int,
    var recoActivatedNumber: Int
) : Parcelable

data class MainGroupChartData(
    var groupName: String,
    var groupNumber: Float
)