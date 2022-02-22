package com.leesangmin89.readcontacttest.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import com.leesangmin89.readcontacttest.data.entity.ContactInfo
import com.leesangmin89.readcontacttest.data.dao.ContactInfoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val database: ContactInfoDao,
    private val dataContact: ContactDao,
    application: Application
) : AndroidViewModel(application) {

    val infoData: LiveData<ContactInfo>

    val _contactNumbers = MutableLiveData<Int>()
    val contactNumbers: LiveData<Int> = _contactNumbers

    private val _progressBarEventFinished = MutableLiveData<Boolean>()
    val progressBarEventFinished : LiveData<Boolean> = _progressBarEventFinished

    init {
        infoData = database.getRecentData()
    }

    fun insertInfo(
        contactNumber: Int,
        activatedContact: Int,
        mostRecentContact: String,
        mostContactName: String,
        mostContactTimes: Int
    ) {
        viewModelScope.launch {
            val insertName = dataContact.getName(mostContactName)
            val insertList = ContactInfo(contactNumber, activatedContact, mostRecentContact, insertName, mostContactTimes)
            database.insert(insertList)
            _progressBarEventFinished.value = true
        }
    }

    fun contactInfoDataClear() {
        viewModelScope.launch {
            database.clear()
        }
    }

    fun progressBarEventReset() {
        _progressBarEventFinished.value = false
    }
}