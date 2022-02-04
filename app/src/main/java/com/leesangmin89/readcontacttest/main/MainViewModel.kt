package com.leesangmin89.readcontacttest.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.data.entity.ContactInfo
import com.leesangmin89.readcontacttest.data.dao.ContactInfoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val database: ContactInfoDao,
    application: Application
) : AndroidViewModel(application)  {

    val infoData : LiveData<ContactInfo>

    val _contactNumbers = MutableLiveData<Int>()
    val contactNumbers : LiveData<Int> = _contactNumbers


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