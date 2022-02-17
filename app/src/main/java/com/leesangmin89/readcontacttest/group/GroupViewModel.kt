package com.leesangmin89.readcontacttest.group

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.MyApplication
import com.leesangmin89.readcontacttest.data.dao.CallLogDao
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import com.leesangmin89.readcontacttest.data.dao.GroupListDao
import com.leesangmin89.readcontacttest.data.entity.GroupList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val database: ContactDao,
    private val dataGroup: GroupListDao,
    private val dataCall: CallLogDao,
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

    private val _coroutineDoneEvent = MutableLiveData<Boolean>()
    val coroutineDoneEvent: LiveData<Boolean> = _coroutineDoneEvent

    private val _getOnlyGroupNameDoneEvent = MutableLiveData<Boolean>()
    val getOnlyGroupNameDoneEvent: LiveData<Boolean> = _getOnlyGroupNameDoneEvent

    private val _groupListGetEvent = MutableLiveData<Boolean>()
    val groupListGetEvent: LiveData<Boolean> = _groupListGetEvent

    private val _groupListUpdateEvent = MutableLiveData<Boolean>()
    val groupListUpdateEvent: LiveData<Boolean> = _groupListUpdateEvent


    init {
    }

    // 그룹이 존재하는 리스트를 정리하여 recyclerview 에 알맞게 가공하는 함수
    fun getGroupName() {
        viewModelScope.launch {
            // 그룹 이름이 있는 것들을 리스트 형태로 모은 변수
//            val data = database.getGroupName()
            val data = dataGroup.getGroupName()
            val emptyData = emptyList<ContactBase>()
            // GroupData 를 넣을 빈 리스트 변수
            val groupData = mutableListOf<GroupData>()

            val groupDataMap = mutableMapOf<String, Int>()

            // 그룹명을 리스트 형태로 모아서, 그룹당 사람 수를 출력하는 코드
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

                // 그룹 등록이 된 연락처 총수
                val groupNameToSumOfNumbers: Int =
                    groupDataMap.map { it.value }.sum()

                // groupDataMap 을 순회하면서
                for ((name, numbers) in groupDataMap) {
                    val rateNumber : Double = (numbers.toDouble() / groupNameToSumOfNumbers.toDouble()) * 100
                    val rate = "등록비율 : ${String.format("%.0f",rateNumber)}%      (${numbers}/${groupNameToSumOfNumbers})"
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
            _newGroupList.value = dataGroup.getGroupList(key)
            _groupListGetEvent.value = true
        }
    }

    // 특정 그룹의 리스트를 반복하면서, 번호를 넘겨 가장 최근 통화일자, 시간, 유형을 받아오는 함수
    fun updateGroupRecentInfo(key: String) {
        viewModelScope.launch {
            val groupList: List<GroupList>? = _newGroupList.value
            if (groupList != null) {
                for (item in groupList) {
                    val newInfo = dataCall.getDDC(item.number)
                    if (newInfo != null) {
                        val newList = GroupList(
                            item.name,
                            item.number,
                            item.group,
                            item.image,
                            newInfo.date,
                            newInfo.duration,
                            item.id
                        )
                        dataGroup.update(newList)
                    } else {
                        // 해당 번호와의 통화기록이 없으면 빈칸을 반환한다.
                        val newList = GroupList(
                            item.name,
                            item.number,
                            item.group,
                            item.image,
                            "",
                            "",
                            item.id
                        )
                        dataGroup.update(newList)
                    }
                }
            }
            _newGroupList.value = dataGroup.getGroupList(key)
            _groupListUpdateEvent.value = true
            _groupListGetEvent.value = false
        }
    }


    fun groupListEmptyChecked() {
        _groupListEmptyEvent.value = false
    }

    fun insert(groupList: GroupList) {
        viewModelScope.launch {
            dataGroup.insert(groupList)
        }
    }

    fun update(groupList: GroupList) {
        viewModelScope.launch {
            dataGroup.update(groupList)
        }
    }

    fun delete(groupList: GroupList) {
        viewModelScope.launch {
            dataGroup.delete(groupList)
        }
    }

    fun clear() {
        viewModelScope.launch {
            dataGroup.clear()
        }
    }

    fun clearByGroupName(groupName: String) {
        viewModelScope.launch {
            dataGroup.deleteByGroupName(groupName)
        }
    }

    fun clearGroupNameInContactBase(groupName: String) {
        viewModelScope.launch {
            for (data in database.getAllContactBaseList()) {
                if (data.group == groupName) {
                    val newData = ContactBase(data.name, data.number, "", data.image, data.id)
                    database.update(newData)
                }
            }
            _coroutineDoneEvent.value = true
        }
    }

    fun coroutineDoneEventFinished() {
        _coroutineDoneEvent.value = false
    }



    fun findAndDelete(number: String) {
        viewModelScope.launch {
            val groupListForDelete = dataGroup.getGroupByNumber(number)
            dataGroup.delete(groupListForDelete)
        }
    }

    fun findAndUpdate(name: String, number: String, group: String) {
        viewModelScope.launch {
            val groupListForUpdate = dataGroup.getGroupByNumber(number)
            val updateList = GroupList(
                name,
                number,
                group,
                groupListForUpdate.image,
                groupListForUpdate.recentContact,
                groupListForUpdate.recentContactCallTime,
                groupListForUpdate.id
            )
            dataGroup.update(updateList)
        }
    }

    // 그룹명 만 리스트 형태로 출력하는 함수
    fun getOnlyGroupName() {
        viewModelScope.launch {
            // DB 로부터 그룹 명만 리스트 형태로 받아
            val groupNameList = dataGroup.getGroupName().distinct()
            val jsonList = JSONArray()
//            "그룹명", "직접입력"
            jsonList.put("그룹명 선택")
            jsonList.put("직접입력")
            for (i in groupNameList) {
                jsonList.put(i)
            }
            val saveList = jsonList.toString()
            // SharedPreferences 에 저장
            MyApplication.prefs.setString("group", saveList)
            _getOnlyGroupNameDoneEvent.value = true
        }
    }
}