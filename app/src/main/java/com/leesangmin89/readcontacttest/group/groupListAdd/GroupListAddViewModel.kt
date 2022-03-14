package com.leesangmin89.readcontacttest.group.groupListAdd

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.leesangmin89.readcontacttest.data.dao.CallLogDao
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import com.leesangmin89.readcontacttest.data.dao.GroupListDao
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.entity.GroupList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupListAddViewModel @Inject constructor(
    private val database: ContactDao,
    private val dataGroup: GroupListDao,
    private val dataCall: CallLogDao,
    application: Application
) : AndroidViewModel(application) {

    private val _navigateEvent = MutableLiveData<Boolean>()
    val navigateEvent : LiveData<Boolean> = _navigateEvent

    lateinit var liveList: LiveData<List<ContactBase>>

    init {
    }


    fun initSort(groupName: String) {
        liveList = database.getDataExceptArgsGroup(groupName)
    }

    fun searchDatabase(searchQuery: String): LiveData<List<ContactBase>> {
        return database.searchDatabase(searchQuery).asLiveData()
    }

    // 특정 정보를 넘겨 가장 최근 통화일자, 시간, 유형을 받아와 insert 하는 함수
    fun insertGroupRecentInfo(groupList:GroupList) {
        viewModelScope.launch {
            val newInfo = dataCall.getDDC(groupList.number)
            if (newInfo != null) {
                val newList = GroupList(
                    groupList.name,
                    groupList.number,
                    groupList.group,
                    groupList.image,
                    newInfo.date,
                    newInfo.duration,
                    groupList.recommendation,
                    0
                )
                dataGroup.insert(newList)
            } else {
                // 해당 번호와의 통화기록이 없으면 빈칸을 반환한다.
                val newList = GroupList(
                    groupList.name,
                    groupList.number,
                    groupList.group,
                    groupList.image,
                    "",
                    "",
                    groupList.recommendation,
                    0
                )
                dataGroup.insert(newList)

            }
        }
    }

    fun updateContactBase(updateList: ContactBase) {
        viewModelScope.launch {
            database.update(updateList)
        }
    }

    fun navigateActive() {
        _navigateEvent.value = true
    }

    fun navigateDone() {
        _navigateEvent.value = false
    }

    fun addGroupListData(list: List<ContactBase>, groupName : String) {
        viewModelScope.launch {
            // 리스트 순회하면서 데이터 DB 업데이트
            for (item in list) {
                // 1. 업데이트 data to ContactBase DB
                val updateList =
                    ContactBase(item.name, item.number, groupName, item.image, item.id)
                updateContactBase(updateList)

                // 2. 업데이트 data to GroupList DB
                val newGroupList =
                    GroupList(item.name, item.number, groupName, item.image, "", "",false, 0)

                if (item.group == "") {
                    // 기존 GroupList 없는 경우,
                    val newInfo = dataCall.getDDC(newGroupList.number)
                    if (newInfo != null) {
                        val newList = GroupList(
                            newGroupList.name,
                            newGroupList.number,
                            newGroupList.group,
                            newGroupList.image,
                            newInfo.date,
                            newInfo.duration,
                            newGroupList.recommendation,
                            0
                        )
                        dataGroup.insert(newList)
                    } else {
                        // 해당 번호와의 통화기록이 없으면 빈칸을 반환한다.
                        val newList = GroupList(
                            newGroupList.name,
                            newGroupList.number,
                            newGroupList.group,
                            newGroupList.image,
                            "",
                            "",
                            newGroupList.recommendation,
                            0
                        )
                        dataGroup.insert(newList)

                    }
                } else {
                    // 기존 GroupList 에 다른 그룹으로 존재하는 경우,
                    // 번호를 받아서, 기존 groupList 에서 가져옴
                    val preGroupList = dataGroup.getGroupByNumber(newGroupList.number)!!
                    // 번호를 받아서, 최근 통화 정보를 가져옴
                    val newInfo = dataCall.getDDC(newGroupList.number)
                    if (newInfo != null) {
                        val newList = GroupList(
                            preGroupList.name,
                            preGroupList.number,
                            newGroupList.group,
                            preGroupList.image,
                            newInfo.date,
                            newInfo.duration,
                            preGroupList.recommendation,
                            preGroupList.id
                        )
                        dataGroup.update(newList)
                    } else {
                        // 해당 번호와의 통화기록이 없으면 빈칸을 반환한다.
                        val newList = GroupList(
                            preGroupList.name,
                            preGroupList.number,
                            newGroupList.group,
                            preGroupList.image,
                            "",
                            "",
                            preGroupList.recommendation,
                            preGroupList.id
                        )
                        dataGroup.update(newList)
                    }
                }
            }
            navigateActive()
        }
    }


}