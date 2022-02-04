package com.leesangmin89.readcontacttest.group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.data.ContactBase
import com.leesangmin89.readcontacttest.data.ContactDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val database: ContactDao,
    application: Application
) : AndroidViewModel(application) {

    private val _groupInfo = MutableLiveData<List<GroupData>>()
    val groupInfo: LiveData<List<GroupData>> = _groupInfo

    private val _groupList = MutableLiveData<List<ContactBase>>()
    val groupList: LiveData<List<ContactBase>> = _groupList

    private val _groupListEmptyEvent = MutableLiveData<Boolean>()
    val groupListEmptyEvent : LiveData<Boolean> = _groupListEmptyEvent

    init {
        getGroupName()
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

    // 그룹명을 매개변수로 하여, 해당 그룹 리스트를 가져오는 함수
    fun getGroupList(key: String) {
        viewModelScope.launch {
            _groupList.value = database.getGroupList(key)
        }
    }

    fun groupListEmptyChecked() {
        _groupListEmptyEvent.value = false
    }
}