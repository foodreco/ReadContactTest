package com.leesangmin89.readcontacttest.list

import android.app.Application
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.*
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import com.leesangmin89.readcontacttest.data.dao.GroupListDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val database: ContactDao,
    private val dataGroup: GroupListDao,
    application: Application
) : AndroidViewModel(application) {

    private var _initializeContactEvent = MutableLiveData<Boolean>()
    val initializeContactEvent: LiveData<Boolean> = _initializeContactEvent

    private var _sortEvent = MutableLiveData<Int>()
    var sortEvent: LiveData<Int> = _sortEvent

    var listData: LiveData<List<ContactBase>>
    var listDataNameDESC: LiveData<List<ContactBase>>
    var listDataNumberASC: LiveData<List<ContactBase>>


    init {
        listData = database.getAllDataByNameASC()
        listDataNameDESC = database.getAllDataByNameDESC()
        listDataNumberASC = database.getAllDataByNumberASC()

        // 최초 정렬 기본값(0) 주기
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
    }

    fun getAllDataByDESC() {
        _sortEvent.value = 1
    }

    fun getAllDataByNumberASD() {
        _sortEvent.value = 2
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

    // 전화번호를 매개변수로 하여, GroupList 에서 해당 그룹 리스트를 가져오는 함수
    fun find(number: String) {
        viewModelScope.launch {
            val groupListForFind = dataGroup.getGroupByNumber(number)
            groupListForFind.group
        }
    }

    fun updateGroupNameInContactBase() {
        viewModelScope.launch {
            for (contact in database.getAllContactBaseList()) {
                for (groupList in dataGroup.getAllDataFromGroupList()) {
                    if (contact.number == groupList.number) {
                        val updateList = ContactBase(
                            contact.name,
                            contact.number,
                            groupList.group,
                            contact.image,
                            contact.id
                        )
                        database.update(updateList)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}