package com.leesangmin89.readcontacttest.callLog

import android.app.Application
import androidx.lifecycle.*
import com.leesangmin89.readcontacttest.CallLogItem
import com.leesangmin89.readcontacttest.convertLongToDateString
import com.leesangmin89.readcontacttest.data.dao.CallLogDao
import com.leesangmin89.readcontacttest.data.dao.TendencyDao
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val dataCallLog: CallLogDao,
    private val dataTen: TendencyDao,
    application: Application
) : AndroidViewModel(application) {

    private val _dialogDismissEvent = MutableLiveData<Boolean>()
    val dialogDismissEvent : LiveData<Boolean> = _dialogDismissEvent

    private val _callLogItemData = MutableLiveData<List<CallLogItem>>()
    val callLogItemData : LiveData<List<CallLogItem>> = _callLogItemData

    init {
    }

    fun insert(callLogData: CallLogData) {
        viewModelScope.launch {
            dataCallLog.insert(callLogData)
        }
    }

    fun groupDetailList(number: String) : LiveData<List<CallLogData>> {
        return dataCallLog.groupDetailList(number).asLiveData()
    }
    fun groupDetailImportanceList(number: String) : LiveData<List<CallLogData>> {
        return dataCallLog.groupDetailImportanceList(number).asLiveData()
    }

    fun sortByNormal(): LiveData<List<CallLogData>> {
        return dataCallLog.sortByNormal().asLiveData()
    }

    fun sortByContact(): LiveData<List<CallLogData>> {
        return dataCallLog.sortByContact().asLiveData()
    }

    fun sortByImportance(): LiveData<List<CallLogData>> {
        return dataCallLog.sortByImportance().asLiveData()
    }


    fun updateCallContent(callLogData: CallLogData) {
        viewModelScope.launch {
            dataCallLog.update(callLogData)
            _dialogDismissEvent.value = true
        }
    }

    fun diaLogDismissDone() {
        _dialogDismissEvent.value = false
    }

    // DB 에서 가져온 리스트 가공 (미리 날짜별로 정렬한 리스트를 가져와야 함)
    private fun List<CallLogData>.toListItems(): List<CallLogItem> {
        val result = arrayListOf<CallLogItem>() // 결과를 리턴할 리스트
        var groupHeaderDate = "" // 그룹날짜
        this.forEach { callLog ->
            // 날짜가 달라지면 그룹헤더를 추가.
            if (groupHeaderDate != callLog.date?.let { convertLongToDateString(it.toLong()) }) {
                result.add(CallLogItem.Header(callLog))
            }

            // 그때의 CallLogData 추가.
            result.add(CallLogItem.Item(callLog))

            // 그룹날짜를 바로 이전 날짜로 설정.
            groupHeaderDate = callLog.date?.let { convertLongToDateString(it.toLong()) }.toString()
        }
        return result
    }

    fun makeList(callLogData: List<CallLogData>) {
        val listItems = callLogData.toListItems()
        _callLogItemData.postValue(listItems)
    }



}