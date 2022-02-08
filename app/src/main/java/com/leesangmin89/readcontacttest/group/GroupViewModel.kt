package com.leesangmin89.readcontacttest.group

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.PrimaryKey
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import com.leesangmin89.readcontacttest.data.dao.GroupListDao
import com.leesangmin89.readcontacttest.data.entity.GroupList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.acl.Group
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val database: ContactDao,
    private val datagroup: GroupListDao,
    application: Application
) : AndroidViewModel(application) {

    private val _groupInfo = MutableLiveData<List<GroupData>>()
    val groupInfo: LiveData<List<GroupData>> = _groupInfo

    private val _groupList = MutableLiveData<List<ContactBase>>()
    val groupList: LiveData<List<ContactBase>> = _groupList

    private val _groupListEmptyEvent = MutableLiveData<Boolean>()
    val groupListEmptyEvent: LiveData<Boolean> = _groupListEmptyEvent

    private val _newGroupList = MutableLiveData<List<GroupList>>()
    val newGroupList: LiveData<List<GroupList>> = _newGroupList

    init {
        getGroupName()
        Log.d("확인", "그룹 뷰모델 초기화됨")
    }

    // 그룹이 존재하는 리스트를 정리하여 recyclerview 에 알맞게 가공하는 함수
    private fun getGroupName() {
        viewModelScope.launch {
            // 그룹 이름이 있는 것들을 리스트 형태로 모은 변수
            val data = database.getGroupName()
            val emptyData = emptyList<ContactBase>()
            // GroupData 를 넣을 빈 리스트 변수
            val groupData = mutableListOf<GroupData>()

            val groupDataMap = mutableMapOf<String, Int>()

            // 만약 data 가 empty 상태라면
            if (data == emptyData) {
                _groupListEmptyEvent.value = true
            } else {
                // data 가 empty 가 아니라면
                // data 를 순회하면서
                for (groupName in data) {
                    if (groupDataMap.contains(groupName)) {
                        val preValue = groupDataMap[groupName]
                        if (preValue != null) {
                            groupDataMap[groupName] = preValue + 1
                        }
                    } else {
                        groupDataMap[groupName] = 1
                    }
                }
                // groupDataMap 을 순회하면서
                for ((name, numbers) in groupDataMap) {
                    val rate = "등록비율 : ${(numbers / 2000) * 100}%      (${numbers}/2000)"
                    val regiNumbers = "등록인원 수 : ${numbers}명"
                    val list = GroupData(name, regiNumbers, rate)
                    groupData.add(list)
                }
                _groupInfo.value = groupData
            }
        }
    }

    // 그룹명을 매개변수로 하여, ContactBase에서 해당 그룹 리스트를 가져오는 함수
    fun getGroupListFromContactBase(key: String) {
        viewModelScope.launch {
            _groupList.value = database.getGroupList(key)
        }
    }

    // 그룹명을 매개변수로 하여, GroupList 에서 해당 그룹 리스트를 가져오는 함수
    fun getGroupListFromGroupList(key: String) {
        viewModelScope.launch {
            _newGroupList.value = datagroup.getGroupList(key)
        }
    }

    fun groupListEmptyChecked() {
        _groupListEmptyEvent.value = false
    }

    fun insert(groupList: GroupList) {
        viewModelScope.launch {
            datagroup.insert(groupList)
        }
    }

    fun update(groupList: GroupList) {
        viewModelScope.launch {
            datagroup.update(groupList)
        }
    }

    fun delete(groupList: GroupList) {
        viewModelScope.launch {
            datagroup.delete(groupList)
        }
    }

    fun clear() {
        viewModelScope.launch {
            datagroup.clear()
        }
    }

    // 전화번호를 매개변수로 하여, GroupList 에서 해당 그룹 리스트를 가져오는 함수
    fun find(number: String) {
        viewModelScope.launch {
            val groupListForFind = datagroup.getGroupByNumber(number)
        }
    }

    fun findAndDelete(number: String) {
        viewModelScope.launch {
            val groupListForDelete = datagroup.getGroupByNumber(number)
            datagroup.delete(groupListForDelete)
        }
    }

    fun findAndUpdate(name: String, number: String, group: String) {
        viewModelScope.launch {
            val groupListForUpdate = datagroup.getGroupByNumber(number)
            val updateList = GroupList(
                name,
                number,
                group,
                groupListForUpdate.image,
                groupListForUpdate.recentContact,
                groupListForUpdate.recentContactCallTime,
                groupListForUpdate.id
            )
            datagroup.update(updateList)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("확인", "뷰모델 파괴됨")
    }
}