package com.leesangmin89.readcontacttest.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.data.ContactInfo
import com.leesangmin89.readcontacttest.data.ContactInfoDao
import kotlinx.coroutines.launch

class MainViewModel(
    private val database: ContactInfoDao,
    application: Application
) : AndroidViewModel(application)  {

    val infoData : LiveData<ContactInfo>

    init {
        infoData = database.getRecentData()
    }

    fun insertInfo(contactInfo: ContactInfo) {
        viewModelScope.launch {
            database.insert(contactInfo)
        }
    }

    fun clear() {
        viewModelScope.launch {
            database.clear()
        }
    }

}