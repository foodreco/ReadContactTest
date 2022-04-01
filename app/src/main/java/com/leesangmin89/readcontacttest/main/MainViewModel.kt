package com.leesangmin89.readcontacttest.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.leesangmin89.readcontacttest.ContactSpl
import com.leesangmin89.readcontacttest.data.dao.*
import com.leesangmin89.readcontacttest.data.entity.ContactInfo
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.entity.Tendency
import com.leesangmin89.readcontacttest.group.GroupData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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


    fun getInfoData(): LiveData<ContactInfo> {
        return dataInfo.getRecentDataFlow().asLiveData()
    }


    // ContactInfo 를 insert 하는 함수
    // 단, contactBase 가 먼저 구성되어 있어야 한다.
    fun insertInfo(
        contactNumber: Int,
        activatedContact: Int,
        mostRecentContact: String,
        mostContactName: String,
        mostContactTimes: Int
    ) {
        viewModelScope.launch {
            // contactBase 에 해당 번호가 등록되어 있으면, name 을
            // 등록되어 있지 않으면 number 를 인자로 해서 insert 하는 함수
            val insertName = dataBase.getName(mostContactName)
            if (insertName != null) {
                val insertList = ContactInfo(
                    contactNumber,
                    activatedContact,
                    mostRecentContact,
                    insertName,
                    mostContactTimes
                )
                dataInfo.insert(insertList)
            } else {
                val insertList = ContactInfo(
                    contactNumber,
                    activatedContact,
                    mostRecentContact,
                    mostContactName,
                    mostContactTimes
                )
                dataInfo.insert(insertList)
            }
            _progressBarEventFinished.value = true
        }
    }

    fun contactInfoDataClear() {
        viewModelScope.launch {
            dataInfo.clear()
        }
    }

    fun progressBarEventDone() {
        _progressBarEventFinished.value = true
    }

    fun progressBarEventReset() {
        _progressBarEventFinished.value = false
    }

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
                        contactList.image = MediaStore.Images.Media.getBitmap(
                            activity.contentResolver,
                            Uri.parse(photoUri)
                        )
//                    val source = ImageDecoder.createSource(activity.contentResolver, photoUri)
//                    val bitmap = ImageDecoder.decodeBitmap(source)
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

                // 필요시만 갱신
                // CallLogData 통화기록 데이터 갱신
                // 없는 기록만 insert, Tendency DB update
                val check = dataCall.confirmAndInsert(number, date, duration)
                if (check == null) {
                    // 기존에 없는 CallLog 가 발생하면 insert
                    val callLogListChild = CallLogData(name, number, date, duration, callType)
                    dataCall.insert(callLogListChild)

                    // 추가로 Tendency AllCall 관련 데이터도 업데이트,
                    val preTendency = dataTen.getRecentTendency()
                    if (preTendency != null) {

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

    // contactInfo data 를 매번 업데이트 하는 함수
    // 매번 갱신
    @SuppressLint("Range")
    fun contactInfoUpdate(activity: Activity) {
        // 전화 로그 가져오는 uri
        val callLogUri = CallLog.Calls.CONTENT_URI
        // contactInfo 변수
        var callCountNum = 0
        var activatedContact = 0
        val list = mutableListOf<ContactSpl>()
        val contactMap = mutableMapOf<String, Int>()

        val contacts = activity.contentResolver.query(
            callLogUri,
            null,
            null,
            null,
            null
        )

        // 데이터 중첩을 막기 위해, 기존 데이터 삭제
        contactInfoDataClear()

        while (contacts!!.moveToNext()) {
            val id =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
            var name =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.CACHED_NAME))
            val number =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.NUMBER))
            val duration =
                contacts.getString(contacts.getColumnIndex(CallLog.Calls.DURATION))

            if (name == null) {
                name = number
            }

            // contactInfo 전화 통계 데이터 갱신
            // 59초 이상 통화 -> 유효 통화횟수 추가
            if (duration.toInt() > 59) {
                activatedContact++
            }
            callCountNum++

            val listChild = ContactSpl(id, name, number, duration)
            list.add(listChild)

            // 번호와 누적 통화량을 기록하는 코드
            if (number in contactMap) {
                val preValue = contactMap[number]
                if (preValue != null) {
                    contactMap[number] = preValue + duration.toInt()
                }
            } else {
                contactMap[number] = duration.toInt()
            }
        }
        // 가장 최근 통화 대상(최근 대상부터 while 반복됨)
        val mostRecentContact = list[0].name
        // 가장 최장 통화시간
        val mapMaxValue = contactMap.maxOf { it.value }
        // 가장 최장 통화대상 번호
        val mapMaxKey = contactMap.filterValues { it == mapMaxValue }.keys.first()

        // ContactBase 필요
        insertInfo(
            callCountNum,
            activatedContact,
            mostRecentContact,
            mapMaxKey,
            mapMaxValue
        )
        contacts.close()
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
            var contactNumbers = 0
            var groupListNumbers = 0
            var recoListNumbers = 0

            val allContactBase = dataBase.getNumbersContactBaseList()
            for (number in allContactBase) {
                contactNumbers++
            }

            val allGroupList = dataGroup.getNumberFromGroupList()
            for (number in allGroupList) {
                groupListNumbers++
            }

            val allRecoList = dataReco.getAllNumbers()
            for (number in allRecoList) {
                recoListNumbers++
            }
            _countNumbers.value = CountNumbers(contactNumbers, groupListNumbers, recoListNumbers)
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
//                _mainGroupChartData.value = groupData
            Log.i("중지", "아무것도 안한다고 가정?")
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

data class CountNumbers(
    var contactNumbers: Int,
    var groupNumbers: Int,
    var recoNumbers: Int
)

data class MainGroupChartData(
    var groupName: String,
    var groupNumber: Float
)