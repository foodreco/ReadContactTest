package com.leesangmin89.readcontacttest.callLog

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.data.dao.CallLogDao
import com.leesangmin89.readcontacttest.data.dao.TendencyDao
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.Tendency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val database: CallLogDao,
    private val dataTen: TendencyDao,
    application: Application
) : AndroidViewModel(application) {

    val callLogList: LiveData<List<CallLogData>>

    private val _dialogDismissEvent = MutableLiveData<Boolean>()
    val dialogDismissEvent : LiveData<Boolean> = _dialogDismissEvent

    lateinit var groupDetailList : LiveData<List<CallLogData>>


    init {
        callLogList = database.getAllDataByDate()
    }

    fun findAndReturnLive(phoneNumber : String) {
        groupDetailList = database.findAndReturnLive(phoneNumber)
    }

    fun callLogClear() {
        viewModelScope.launch {
            database.clear()
        }
    }

    fun insert(callLogData: CallLogData) {
        viewModelScope.launch {
            database.insert(callLogData)
        }
    }

    fun confirmAndInsert(
        name: String,
        number: String,
        date: String,
        duration: String,
        callType: String
    ) {
        viewModelScope.launch {
            val check = database.confirmAndInsert(number, date, duration)
            if (check == null) {
                // 기존에 없는 CallLog 가 발생하면 insert
                val callLogListChild = CallLogData(name, number, date, duration, callType)
                database.insert(callLogListChild)

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

                    allCallIncoming  +=  preTendency.allCallIncoming.toInt()
                    allCallOutgoing  +=  preTendency.allCallOutgoing.toInt()
                    allCallMissed  +=  preTendency.allCallMissed.toInt()
                    allCallCount  +=  preTendency.allCallCount.toInt()
                    allCallDuration  +=  preTendency.allCallDuration.toInt()
                    allCallPartnerList = (preTendency.allCallPartnerList + allCallPartnerList) as MutableList<String>

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
    }

    fun updateCallContent(callLogData: CallLogData) {
        viewModelScope.launch {
            database.update(callLogData)
            _dialogDismissEvent.value = true
        }
    }

    fun diaLogDismissDone() {
        _dialogDismissEvent.value = false
    }

}