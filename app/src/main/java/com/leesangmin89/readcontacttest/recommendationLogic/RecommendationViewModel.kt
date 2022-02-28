package com.leesangmin89.readcontacttest.recommendationLogic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
        testList = dataReco.getAllDataByNameASC()
    }


    // 통화를 추천할 대상 5명을 뽑는 함수
    // 1. recommendation true 중 최근 통화가 없으면 추천
    // 2. 연락빈도를 계산하여 초과하면 추천
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
                    var frequencyExcess = false
                    val callDateList = mutableListOf<Long>()
                    val longNow = System.currentTimeMillis()

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

                    // 통화 횟수가 1 이상일 때, 평균 통화시간 계산 코드
                    if (numberOfCalling != 0) {
                        avgCallTime = totalCallTime / numberOfCalling
                    }

                    // 통화 횟수 2 이상일 때, 통화 평균빈도 계산 코드
                    if (numberOfCalling > 1) {
                        var count = callDateList.size
                        var rightCount: Long = (callDateList.size - 1).toLong()
                        var gap = (callDateList[0] - callDateList[count - 1])
                        frequency = gap / rightCount
                    }

                    // 최근 연락일자가 있으면, 빈도 초과여부를 반환하는 코드
                    if (groupList.recentContact != "") {
                        val recentTimeGap = longNow - groupList.recentContact!!.toLong()
                        val frequencyAVG = frequency.toLong()
                        if (recentTimeGap > frequencyAVG) {
                            frequencyExcess = true
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
                        frequencyExcess,
                        0
                    )

                    // Recommendation DB를 검색하여, 없으면 insert, 있으면 update 하는 코드
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