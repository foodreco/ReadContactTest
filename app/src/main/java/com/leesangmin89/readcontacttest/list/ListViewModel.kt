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

    private var _listData = MutableLiveData<List<ContactBase>>()
    var listData: LiveData<List<ContactBase>> = _listData

    //    var listData: LiveData<List<ContactBase>>
    var listDataNameDESC: LiveData<List<ContactBase>>
    var listDataNumberASC: LiveData<List<ContactBase>>


    init {
        initSort()
        Log.i("확인", "뷰모델 초기화")
//        listData = database.getAllDataByNameASC()
        listDataNameDESC = database.getAllDataByNameDESC()
        listDataNumberASC = database.getAllDataByNumberASC()

        // 최초 정렬 기본값(0) 주기
        _sortEvent.value = 0
    }

    fun initSort() {
        viewModelScope.launch {
            _listData.value = database.getAllDataByNameASCTest()
        }
        Log.i("확인", "initSort / _listData.value = database.getAllDataByNameASCTest()")
    }


    fun insert(contact: ContactBase) {
        viewModelScope.launch {
            database.insert(contact)
        }
    }

    fun clear() {
        viewModelScope.launch {
            database.clear()
            // DB clear 후 정렬 다시 초기화
            initSort()
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
        initSort()
        Log.i("확인", "getAllDataByASC")
    }

    fun getAllDataByDESC() {
        viewModelScope.launch {
            _listData.value = database.getAllDataByNameDESCTest()
        }
        Log.i("확인", "getAllDataByDESC")

    }

    fun getAllDataByNumberASC() {
        viewModelScope.launch {
            _listData.value = database.getAllDataByNumberASCTest()
        }
        Log.i("확인", "getAllDataByNumberASC")
    }

    fun syncWithGroupList() {
        // 연락처를 다시 가져올 때마다, 연락처-그룹 동기화 하는 코드
        updateGroupNameInContactBase()
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
            // 처음 불러오고 다시 초기화해줌
            initSort()
            _initializeContactEvent.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
    }


}