package com.leesangmin89.readcontacttest.recommendationLogic

import android.app.Application
import android.util.Log
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.convertLongToDateString
import com.leesangmin89.readcontacttest.data.dao.*
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.data.entity.Recommendation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


// 추천 로직을 설계한 통합 뷰모델
@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val dataBase: ContactDao,
    private val dataCallLog: CallLogDao,
    private val dataContactInfo: ContactInfoDao,
    private val dataGroup: GroupListDao,
    private val dataReco: RecommendationDao,
    application: Application
) : AndroidViewModel(application) {

    private val _callRecommendation = MutableLiveData<List<ContactBase>>()
    val callRecommendation: LiveData<List<ContactBase>> = _callRecommendation

    private val _callTypeTendency = MutableLiveData<List<ContactBase>>()
    val callTypeTendency: LiveData<List<ContactBase>> = _callTypeTendency

    private val _callStyleTendency = MutableLiveData<List<ContactBase>>()
    val callStyleTendency: LiveData<List<ContactBase>> = _callStyleTendency

    private val _progressBarEventFinished = MutableLiveData<Boolean>()
    val progressBarEventFinished: LiveData<Boolean> = _progressBarEventFinished

    val testList: LiveData<List<Recommendation>>

    init {
        testList = dataReco.getAllDataByRecommended(true)
    }

    // 통화를 추천할 대상을 뽑는 함수
    // 실행 시 매번 업데이트 되어야 함
    // GroupList recommendation true 에 한해서 추론
    // 1. 통화횟수가 0회 일 때, 추천 (ref1)
    // 2. 통화횟수가 1회 일 때, 해당 통화일자가 1년을 초과하면 추천(ref2)
    // 3. 통화횟수가 2회 이상 일 때, 연락빈도를 계산하여 초과하면 추천(ref3)
    // 기타 추가
    // : 중요도 설정에 따라 추천 기준 달리한다?
    // : 사용자 특성(ex.비즈니스 맨의 경우, 마지막 통화가 최근이다.)에 따라, 설정 달리...
    fun arrangeRecommendation() {
        viewModelScope.launch {

            //1. Recommendation = true 인 GroupList 를 불러온다.
            val recommendedGroupList: List<GroupList>? = dataGroup.getRecommendationGroupList(true)

            //2. Recommendation = true 인 GroupList 를 순회하며, 정보를 수집한다.
            if (recommendedGroupList != null) {
                for (groupList in recommendedGroupList) {

                    var totalCallTime: Long = 0
                    var numberOfCalling = 0
                    var avgCallTime: Long = 0
                    var frequency: Long = 0
                    var numberOfCallingBelow = false
                    var recentCallExcess = false
                    var frequencyExcess = false
                    val callDateList = mutableListOf<Long>()

                    val longNow : Long = System.currentTimeMillis()

                    //3. 해당 GroupList 에 해당하는 CallLogData 를 다 가져온다.
                    val callLogData: List<CallLogData>? =
                        dataCallLog.getCallLogDataByNumber(groupList.number)

                    //4. 가져온 CallLogData 를 순회하면서 Recommendation 인자를 수집한다.
                    if (callLogData != null) {
                        for (recoInfo in callLogData) {
                            numberOfCalling++
                            totalCallTime =
                                recoInfo.duration?.let { totalCallTime.plus(it.toInt()) }!!
                            recoInfo.date?.let { callDateList.add(it.toLong()) }
                        }
                    }
                    //4-1. 통화횟수에 따른 Reco DB 인자 설정
                    when (numberOfCalling) {
                        // 0회 일 때, Ref1 true
                        0 -> {
                            numberOfCallingBelow = true
                        }

                        // 통화 횟수가 1 이상일 때,
                        // 해당 통화일자가 1년을 초과하면 추천(ref2)
                        1 -> {
                            avgCallTime = totalCallTime / numberOfCalling
                            val recentTimeGap : Long = longNow - groupList.recentContact!!.toLong()
                            val aYearToLong: Long = 31536000000
                            recentCallExcess = recentTimeGap >= aYearToLong
                        }

                        // 통화 횟수가 2 이상일 때,
                        // 연락빈도를 계산하여 초과하면 추천(ref3)
                        else -> {
                            avgCallTime = totalCallTime / numberOfCalling
                            //frequency(평균빈도) 계산 코드
                            val count = callDateList.size
                            val rightCount: Long = (callDateList.size - 1).toLong()
                            val gap = (callDateList[0] - callDateList[count - 1])
                            frequency = gap / rightCount
                            // 빈도 초과여부를 반환하는 코드
                            val recentTimeGap = longNow - groupList.recentContact!!.toLong()
                            frequencyExcess = recentTimeGap > frequency

                            Log.i("확인","${groupList.name} recentTimeGap : $recentTimeGap")
                            Log.i("확인","${groupList.name} frequency : $frequency")


                        }
                    }

                    val insertRecommendation = Recommendation(
                        groupList.name,
                        groupList.number,
                        groupList.group,
                        groupList.recentContact,
                        totalCallTime.toString(),
                        numberOfCalling.toString(),
                        avgCallTime.toString(),
                        frequency.toString(),
                        numberOfCallingBelow,
                        recentCallExcess,
                        frequencyExcess,
                        0
                    )
                    Log.i("확인","${groupList.name} : $insertRecommendation")

                    // 5. Recommendation DB를 구성하는 코드
                    // Recommendation DB를 검색하여, 없으면 insert, 있으면 update
                    if (dataReco.confirm(groupList.number) != null) {
                        dataReco.update(insertRecommendation)
                    } else {
                        dataReco.insert(insertRecommendation)
                    }
                }
            }
            _progressBarEventFinished.value = true
        }
    }

    fun progressBarEventDone() {
        _progressBarEventFinished.value = false
    }

    // callType 성향을 추론하는 함수
    fun callTypeTendency() {

    }

    // 통화 성향을 추론하는 함수
    fun callStyleTendency() {

    }
}