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
        Log.i("프로그래스바","${_progressBarEventFinished.value}")
    }

    fun insertInfo(
        contactNumber: Int,
        activatedContact: Int,
        mostRecentContact: String,
        mostContactName: String,
        mostContactTimes: Int
    ) {
        viewModelScope.launch {
            Log.d("확인","mostContactName : $mostContactName")
            val insertName = dataContact.getName(mostContactName)
            Log.d("확인","insertName : $insertName")
            val insertList = ContactInfo(contactNumber, activatedContact, mostRecentContact, insertName, mostContactTimes)
            database.insert(insertList)
            _progressBarEventFinished.value = true
            Log.i("프로그래스바","동작 후 : ${_progressBarEventFinished.value}")
        }
    }

    fun clear() {
        viewModelScope.launch {
            database.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("프로그래스바","뷰모델 파괴")
    }

    fun progressBarEventReset() {
        _progressBarEventFinished.value = false
    }
}