package com.leesangmin89.readcontacttest.callLog

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.data.dao.CallLogDao
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val database: CallLogDao,
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
                val callLogListChild = CallLogData(name, number, date, duration, callType)
                database.insert(callLogListChild)
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