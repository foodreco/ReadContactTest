package com.leesangmin89.readcontacttest.callLog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.data.dao.CallLogDao
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import com.leesangmin89.readcontacttest.data.dao.GroupListDao
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val database: CallLogDao,
    application: Application
) : AndroidViewModel(application) {

    val callLogList : LiveData<List<CallLogData>>

    init {
        callLogList = database.getAllDataByDate()
    }

    fun clear() {
        viewModelScope.launch {
            database.clear()
        }
    }

    fun insert(callLogData: CallLogData) {
        viewModelScope.launch {
            database.insert(callLogData)
        }
    }

}