package com.leesangmin89.readcontacttest.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val database: ContactDao,
    application: Application
) : AndroidViewModel(application) {

    private var _initializeContactEvent = MutableLiveData<Boolean>()
    val initializeContactEvent: LiveData<Boolean> = _initializeContactEvent

    private var _sortEvent = MutableLiveData<Int>()
    var sortEvent : LiveData<Int> = _sortEvent

    var listData: LiveData<List<ContactBase>>
    var listDataNameDESC: LiveData<List<ContactBase>>
    var listDataNumberASC: LiveData<List<ContactBase>>


    init {
        listData = database.getAllDataByNameASC()
        listDataNameDESC = database.getAllDataByNameDESC()
        listDataNumberASC = database.getAllDataByNumberASC()
        _sortEvent.value = 0
    }

    fun insert(contact: ContactBase) {
        viewModelScope.launch {
            database.insert(contact)
        }
    }

    fun clear() {
        viewModelScope.launch {
            database.clear()
        }
    }

    fun delete(contact: ContactBase) {
        viewModelScope.launch {
            database.delete(contact)
        }
    }

    fun update(contact: ContactBase) {
        viewModelScope.launch {
            database.update(contact)
        }
    }

    fun getAllDataByASC() {
        _sortEvent.value = 0
        Log.i("확인","getAllDataByASC")
    }

    fun getAllDataByDESC() {
        _sortEvent.value = 1
        Log.i("확인","getAllDataByDESC")
    }

    fun getAllDataByNumberASD() {
        _sortEvent.value = 2
        Log.i("확인","getAllDataByNumberASD")
    }

    fun contactInitCompleted() {
        _initializeContactEvent.value = false
    }

    fun contactActivate() {
        _initializeContactEvent.value = true
    }

    fun searchDatabase(searchQuery: String): LiveData<List<ContactBase>> {
        return database.searchDatabase(searchQuery).asLiveData()
    }




}