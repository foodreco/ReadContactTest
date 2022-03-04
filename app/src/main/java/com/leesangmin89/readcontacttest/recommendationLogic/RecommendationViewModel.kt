package com.leesangmin89.readcontacttest.recommendationLogic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.data.dao.*
import com.leesangmin89.readcontacttest.data.entity.*
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
    private val dataTen: TendencyDao,
    application: Application
) : AndroidViewModel(application) {

    private val _progressBarEventFinished = MutableLiveData<Boolean>()
    val progressBarEventFinished: LiveData<Boolean> = _progressBarEventFinished

    val recommendationLiveList: LiveData<List<Recommendation>>
    var tendencyLive: LiveData<Tendency>?

    init {
        recommendationLiveList = dataReco.getAllDataByRecommended(true)
        tendencyLive = dataTen.getRecentTendencyDataLive()
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
        Log.i("확인","arrangeRecommendation 시작")
        viewModelScope.launch {

            //1. Recommendation = true 인 GroupList 를 불러온다.
            val recommendedGroupList: List<GroupList> = dataGroup.getRecommendationGroupList(true)

            //2. Recommendation = true 인 GroupList 를 순회하며, 정보를 수집한다.
            for (groupList in recommendedGroupList) {

                var totalCallTime: Long = 0
                var numberOfCalling = 0
                var avgCallTime: Long = 0
                var frequency: Long = 0
                var numberOfCallingBelow = false
                var recentCallExcess = false
                var frequencyExcess = false
                val callDateList = mutableListOf<Long>()

                val longNow: Long = System.currentTimeMillis()

                //3. 해당 GroupList 에 해당하는 CallLogData 를 다 가져온다.
                val callLogData: List<CallLogData> =
                    dataCallLog.getCallLogDataByNumber(groupList.number)

                //4. 가져온 CallLogData 를 순회하면서 Recommendation 인자를 수집한다.
                for (recoInfo in callLogData) {
                    numberOfCalling++
                    totalCallTime =
                        recoInfo.duration?.let { totalCallTime.plus(it.toInt()) }!!
                    recoInfo.date?.let { callDateList.add(it.toLong()) }
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
                        val recentTimeGap: Long = longNow - groupList.recentContact!!.toLong()
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
                    }
                }

                // 5. Recommendation DB를 구성하는 코드
                // Recommendation DB를 검색하여, 없으면 insert, 있으면 update
                if (dataReco.confirm(groupList.number) != null) {
                    val updateRecommendation = Recommendation(
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
                        dataReco.confirm(groupList.number)!!.id
                    )
                    dataReco.update(updateRecommendation)
                } else {
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
                    dataReco.insert(insertRecommendation)
                }
            }
            callTendencyForAllCall()
        }
        Log.i("확인","arrangeRecommendation 종료")
    }

    fun progressBarEventDone() {
        _progressBarEventFinished.value = false
    }

    // callType 성향과 통화 성향을 추론하는 함수
    // 수신, 발신, 부재중 비중 도출
    // 평균 통화시간, 통화 상대수
    // 1. 전체 call 에서 정의
    // 2. groupList 관계에서 정의
    // 3. recommendation 관계에서 정의
    fun callTendencyForAllCall() {
        Log.i("확인","callTendencyForAllCall 시작")
        viewModelScope.launch {
            val allTendency: Tendency? = dataTen.getRecentTendency()

            var allCallCount = 0
            var allCallDuration = 0
            var allCallPartnerList = mutableListOf<String>()
            var allCallIncoming = 0
            var allCallOutgoing = 0
            var allCallMissed = 0

            var groupListCallCount = 0
            var groupListCallDuration = 0
            var groupListCallPartnerList = mutableListOf<String>()
            var groupListCallIncoming = 0
            var groupListCallOutgoing = 0
            var groupListCallMissed = 0

            var recommendationCallCount = 0
            var recommendationCallDuration = 0
            var recommendationCallPartnerList = mutableListOf<String>()
            var recommendationCallIncoming = 0
            var recommendationCallOutgoing = 0
            var recommendationCallMissed = 0

            // Tendency 인자 수정
            if (allTendency == null) {
                // 최초 앱 빌드 후(CallLogData 구축 후) 발동 시,
                // 1. 전체 call 기준
                val allCallLogData: List<CallLogData> = dataCallLog.getAllCallLogData()
                // 전체 call 순회
                for (item in allCallLogData) {
                    // 메모리 부하가 크므로, 최초 발동시에만 작업
                    // 기존 없던 통화기록 데이터만, 작업 진행
                    // 총 전화 횟수
                    allCallCount += 1
                    // 총 전화 시간
                    allCallDuration += item.duration!!.toInt()

                    // 통화 유형
                    when (item.callType?.toInt()) {
                        1 -> allCallIncoming += 1
                        2 -> allCallOutgoing += 1
                        3 -> allCallMissed += 1
                        else -> {}
                    }
                    // 통화 상대 추가
                    allCallPartnerList.add(item.number)
                }
                val insertList = Tendency(
                    allCallIncoming.toString(),
                    allCallOutgoing.toString(),
                    allCallMissed.toString(),
                    allCallCount.toString(),
                    allCallDuration.toString(),
                    allCallPartnerList.distinct(),
                    groupListCallIncoming.toString(),
                    groupListCallOutgoing.toString(),
                    groupListCallMissed.toString(),
                    groupListCallCount.toString(),
                    groupListCallDuration.toString(),
                    groupListCallPartnerList.distinct(),
                    recommendationCallIncoming.toString(),
                    recommendationCallOutgoing.toString(),
                    recommendationCallMissed.toString(),
                    recommendationCallCount.toString(),
                    recommendationCallDuration.toString(),
                    recommendationCallPartnerList.distinct(),
                    0
                )
                dataTen.insert(insertList)
                Log.i("확인","insertList : $insertList")
            }
            callTendencyForGroupAndRecoCall()
        }
        Log.i("확인","callTendencyForAllCall 종료")
    }

    // 2. groupList 관계에서 정의
    // 3. recommendation 관계에서 정의
    private fun callTendencyForGroupAndRecoCall() {
        Log.i("확인","callTendencyForGroupAndRecoCall 시작")
        viewModelScope.launch {
            var groupListCallCount = 0
            var groupListCallDuration = 0
            var groupListCallPartnerList = mutableListOf<String>()
            var groupListCallIncoming = 0
            var groupListCallOutgoing = 0
            var groupListCallMissed = 0

            var recommendationCallCount = 0
            var recommendationCallDuration = 0
            var recommendationCallPartnerList = mutableListOf<String>()
            var recommendationCallIncoming = 0
            var recommendationCallOutgoing = 0
            var recommendationCallMissed = 0

            // 2. GroupList 순회 방식
            val allGroupList: List<GroupList> = dataGroup.getAllDataFromGroupList()
            for (groupList in allGroupList) {
                // groupList 를 순회하면서 번호를 넘겨, 해당 통화기록을 다 가져옴
                val callLogDataAll = dataCallLog.getCallLogDataByNumber(groupList.number)
                // 통화 상대 추가
                groupListCallPartnerList.add(groupList.number)
                for (callLogData in callLogDataAll) {
                    // 그룹 전화 횟수
                    groupListCallCount += 1
                    // 그룹 전화 시간
                    groupListCallDuration += callLogData.duration!!.toInt()
                    // 그룹 통화 유형
                    when (callLogData.callType?.toInt()) {
                        1 -> groupListCallIncoming += 1
                        2 -> groupListCallOutgoing += 1
                        3 -> groupListCallMissed += 1
                        else -> {}
                    }
                }
            }

            // 3. Recommendation 순회 방식
            val allRecommendationList: List<Recommendation> = dataReco.getAllData()
            for (recommendation in allRecommendationList) {
                // recommendation 를 순회하면서 번호를 넘겨, 해당 통화기록을 다 가져옴
                val callLogDataAll = dataCallLog.getCallLogDataByNumber(recommendation.number)
                // 통화 상대 추가
                recommendationCallPartnerList.add(recommendation.number)
                for (callLogData in callLogDataAll) {
                    // 추천 전화 횟수
                    recommendationCallCount += 1
                    // 추천 전화 시간
                    recommendationCallDuration += callLogData.duration!!.toInt()
                    // 추천 통화 유형
                    when (callLogData.callType?.toInt()) {
                        1 -> recommendationCallIncoming += 1
                        2 -> recommendationCallOutgoing += 1
                        3 -> recommendationCallMissed += 1
                        else -> {}
                    }
                }
            }

            // 최종 Tendency 인자 수정
            val allTendency: Tendency? = dataTen.getRecentTendency()
            if (allTendency != null) {
                val updateList = Tendency(
                    allTendency.allCallIncoming,
                    allTendency.allCallOutgoing,
                    allTendency.allCallMissed,
                    allTendency.allCallCount,
                    allTendency.allCallDuration,
                    allTendency.allCallPartnerList,
                    groupListCallIncoming.toString(),
                    groupListCallOutgoing.toString(),
                    groupListCallMissed.toString(),
                    groupListCallCount.toString(),
                    groupListCallDuration.toString(),
                    groupListCallPartnerList.distinct(),
                    recommendationCallIncoming.toString(),
                    recommendationCallOutgoing.toString(),
                    recommendationCallMissed.toString(),
                    recommendationCallCount.toString(),
                    recommendationCallDuration.toString(),
                    recommendationCallPartnerList.distinct(),
                    allTendency.id
                )
                dataTen.update(updateList)
                Log.i("확인","updateList : $updateList")
            }
        }
        Log.i("확인","callTendencyForGroupAndRecoCall 종료")
    }
}