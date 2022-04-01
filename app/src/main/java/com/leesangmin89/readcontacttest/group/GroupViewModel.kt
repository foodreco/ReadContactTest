package com.leesangmin89.readcontacttest.group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leesangmin89.readcontacttest.GroupTopData
import com.leesangmin89.readcontacttest.MyApplication
import com.leesangmin89.readcontacttest.data.dao.CallLogDao
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.dao.ContactDao
import com.leesangmin89.readcontacttest.data.dao.GroupListDao
import com.leesangmin89.readcontacttest.data.dao.RecommendationDao
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.group.groupList.GroupItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val database: ContactDao,
    private val dataGroup: GroupListDao,
    private val dataCall: CallLogDao,
    private val dataReco: RecommendationDao,
    application: Application
) : AndroidViewModel(application) {

    private val _groupInfo = MutableLiveData<List<GroupData>>()
    val groupInfo: LiveData<List<GroupData>> = _groupInfo

    private val _groupFragmentList = MutableLiveData<List<GroupData>>()
    val groupFragmentList: LiveData<List<GroupData>> = _groupFragmentList

    private val _groupList = MutableLiveData<List<ContactBase>>()
    val groupList: LiveData<List<ContactBase>> = _groupList

    lateinit var liveGroupLiveData: LiveData<List<GroupList>>

    private val _coroutineDoneEvent = MutableLiveData<Boolean>()
    val coroutineDoneEvent: LiveData<Boolean> = _coroutineDoneEvent

    private val _getOnlyGroupNameDoneEvent = MutableLiveData<Boolean>()
    val getOnlyGroupNameDoneEvent: LiveData<Boolean> = _getOnlyGroupNameDoneEvent

    private val _groupListForSwitch = MutableLiveData<Boolean>()
    val groupListForSwitch: LiveData<Boolean> = _groupListForSwitch

    private val _updateDialogDone = MutableLiveData<Boolean>()
    val updateDialogDone: LiveData<Boolean> = _updateDialogDone

    private val _groupNameEditDone = MutableLiveData<Boolean>()
    val groupNameEditDone: LiveData<Boolean> = _groupNameEditDone

    private val _groupListRecyclerView = MutableLiveData<List<GroupItem>>()
    val groupListRecyclerView: LiveData<List<GroupItem>> = _groupListRecyclerView

    // updateDialog dismiss 를 위한 코드
    private var _updateDataEventNumber = 0
    private var _updateDataEvent = MutableLiveData<Int>(0)
    val updateDataEvent: LiveData<Int> = _updateDataEvent


    init {
    }

    // 그룹이 존재하는 리스트를 정리하여 GroupData 형태로 가공하는 함수
    fun getGroupName() {
        viewModelScope.launch {
            // 그룹 이름이 있는 것들을 리스트 형태로 모은 변수(그룹 생성 순으로 불러온다)
            val data: List<String> = dataGroup.getGroupName()

            // GroupData 를 넣을 빈 리스트 변수
            val groupData = mutableListOf<GroupData>()

            // 맵 : 키-그룹명, 값-사람수
            val groupDataMap = mutableMapOf<String, Int>()

            // 그룹명을 리스트 형태로 모아서, 그룹당 사람 수를 출력하는 코드
            // 만약 data 가 empty 상태라면
            if (data == emptyList<String>()) {
                _groupInfo.value = groupData
            } else {
                // data 가 empty 가 아니라면
                // data 를 순회하면서
                for (groupName in data) {
                    if (groupDataMap.contains(groupName)) {
                        val preValue = groupDataMap[groupName]
                        if (preValue != null) {
                            // 기존 값이 있으면 1을 더하고
                            groupDataMap[groupName] = preValue + 1
                        }
                    } else {
                        // 없으면 1로 시작한다.
                        groupDataMap[groupName] = 1
                    }
                }

                // 그룹 등록이 된 연락처 총수
                val groupNameToSumOfNumbers: Int =
                    groupDataMap.map { it.value }.sum()

                // groupDataMap 을 순회하면서 name : 그룹명, numbers : 사람수
                for ((name, numbers) in groupDataMap) {
                    val numberOfRecommendationTrue: Int =
                        dataGroup.getGroupListRecommendationTrue(name).count()
                    val starRatingInt =
                        ((numberOfRecommendationTrue.toDouble() / numbers.toDouble()) * 5)
                    val list = GroupData(name, numbers, groupNameToSumOfNumbers, numberOfRecommendationTrue, starRatingInt)
                    groupData.add(list)
                }
                _groupInfo.value = groupData
            }
        }
    }

    fun getGroupListFromGroupListByLive(group: String) {
        liveGroupLiveData = dataGroup.getGroupListLive(group)
    }

    // 특정 그룹의 리스트를 반복하면서, 번호를 넘겨 가장 최근 통화일자, 시간, 유형을 받아오는 함수
    fun updateGroupRecentInfo(group: String) {
        viewModelScope.launch {
            val groupList: List<GroupList>? = dataGroup.getGroupList(group)
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
                            item.recommendation,
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
                            item.recommendation,
                            item.id
                        )
                        dataGroup.update(newList)
                    }
                }
            }
        }
    }

    fun insert(groupList: GroupList) {
        viewModelScope.launch {
            dataGroup.insert(groupList)
            _updateDataEventNumber++
            _updateDataEvent.value = _updateDataEventNumber
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

    fun dataRecoDeleteByNumber(number: String) {
        viewModelScope.launch {
            dataReco.deleteByNumber(number)
            updateDialogDone()
        }
    }

    // 그룹명을 받아 해당 그룹에 해당하는 인자를 Reco DB 에서 모두 삭제
    fun dataRecoDeleteByGroup(groupName: String) {
        viewModelScope.launch {
            dataReco.deleteByGroup(groupName)
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
            val groupListForDelete = dataGroup.getGroupByNumber(number)!!
            dataGroup.delete(groupListForDelete)
            _updateDataEventNumber++
            _updateDataEvent.value = _updateDataEventNumber

        }
    }

    fun findAndUpdate(name: String, number: String, group: String, recommendation: Boolean) {
        viewModelScope.launch {
            val groupListForUpdate = dataGroup.getGroupByNumber(number)!!
            val updateList = GroupList(
                name,
                number,
                group,
                groupListForUpdate.image,
                groupListForUpdate.recentContact,
                groupListForUpdate.recentContactCallTime,
                recommendation,
                groupListForUpdate.id
            )
            dataGroup.update(updateList)
            _updateDataEventNumber++
            _updateDataEvent.value = _updateDataEventNumber
        }
    }

    // 그룹명 만 리스트 형태로 출력하는 함수
    fun getOnlyGroupName() {
        viewModelScope.launch {
            // DB 로부터 그룹 명만 리스트 형태로 받아
            val groupNameList = dataGroup.getGroupName().distinct()
            val jsonList = JSONArray()
            // "그룹명", "직접입력"
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


    // 선택 삭제 시 조건부(1.전체삭제 2.부분삭제)로 데이터를 정리하는 함수
    fun arrangeGroupList(testList: List<String>, groupName: String) {
        viewModelScope.launch {
            val newList = mutableListOf<String>()
            newList.addAll(testList)
            val newGroupList = dataGroup.getGroupList(groupName)
            if (newGroupList.count() == newList.count()) {
                //1. 전체 삭제
                clearByGroupName(groupName)
                clearGroupNameInContactBase(groupName)
                dataRecoDeleteByGroup(groupName)
            } else {
                //2. 부분 삭제
                deleteGroupListPart(newList)
            }
        }
    }

    // 선택 삭제 시, 2.부분삭제 시 발동될 코드
    private fun deleteGroupListPart(testList: List<String>) {
        viewModelScope.launch {
            for (number in testList) {
                // 번호를 받아서, ContactBase 정보를 불러와
                val preContactBase = database.getContact(number)
                if (preContactBase != null) {
                    val updateContactBase = ContactBase(
                        preContactBase.name,
                        preContactBase.number,
                        "",
                        preContactBase.image,
                        preContactBase.id
                    )

                    // ContactBase DB group 을 공백으로 업데이트 하는 코드
                    updateInContactBase(updateContactBase)

                    // GroupList 에서 group 을 삭제하는 코드
                    deleteDataInGroupList(number)

                    // Reco DB 에서 해당 정보를 삭제하는 코드
                    deleteDataInReco(number)
                }
            }
        }
    }

    private fun updateInContactBase(contactBase: ContactBase) {
        viewModelScope.launch {
            database.update(contactBase)
        }
    }

    private fun deleteDataInGroupList(number: String) {
        viewModelScope.launch {
            dataGroup.deleteGroupByNumber(number)
        }
    }

    private fun deleteDataInReco(number: String) {
        viewModelScope.launch {
            dataReco.deleteByNumber(number)
        }
    }


    fun checkAlarmState(number: String) {
        viewModelScope.launch {
            val groupList = dataGroup.getGroupByNumber(number)
            if (groupList != null) {
                _groupListForSwitch.value = groupList.recommendation
            } else {
                _groupListForSwitch.value = false
            }
        }
    }

    fun updateDialogDoneFinished() {
        _updateDialogDone.value = false
        _updateDataEventNumber = 0
        _updateDataEvent.value = _updateDataEventNumber
    }

    fun updateDialogDone() {
        _updateDialogDone.value = true
        _updateDataEventNumber++
        _updateDataEvent.value = _updateDataEventNumber
    }

    fun update(contact: ContactBase) {
        viewModelScope.launch {
            database.update(contact)
            _updateDataEventNumber++
            _updateDataEvent.value = _updateDataEventNumber
        }
    }

    // UpdateDialog 에서 적용
    fun updateGroupListDB(argsContactBase: ContactBase, newGroup: String, alarmChecked: Boolean) {
        viewModelScope.launch {
            // 넘어온 Group 이 없을 때,
            if (argsContactBase.group == "") {
                when (newGroup) {
                    // 신규 지정 Group 도 없다면
                    "" -> {
                        _updateDataEventNumber++
                        _updateDataEvent.value = _updateDataEventNumber
                    }
                    // 넘어온 건 없는데, 신규 지정 Group 은 있다면, 그룹 DB 에 신규 추가
                    else -> {
                        val insertList = GroupList(
                            argsContactBase.name,
                            argsContactBase.number,
                            newGroup,
                            argsContactBase.image,
                            "0",
                            "0",
                            alarmChecked,
                            0
                        )
                        insert(insertList)
                    }
                }
            }
            // 넘어온 groupName 이 있을 때(지정된 Group 존재할 때)
            else {
                when (newGroup) {
                    // 수정하여 Group 을 없앨 때, 그룹 DB 에서 해당 List 제거
                    "" -> {
                        findAndDelete(argsContactBase.number)
                    }
                    // 수정하여 Group 을 없애는 것이 아닐 때, 그룹 DB 에서 해당 List 업데이트
                    else -> {
                        findAndUpdate(
                            argsContactBase.name,
                            argsContactBase.number,
                            newGroup,
                            alarmChecked
                        )
                    }
                }
            }

        }
    }

    fun updateData(
        newContactBase: ContactBase,
        preContactBase: ContactBase,
        newGroup: String,
        newRecommendation: Boolean
    ) {
        viewModelScope.launch {
            // 업데이트 data to ContactBase DB
            update(newContactBase)

            // 업데이트 data to GroupList DB
            updateGroupListDB(preContactBase, newGroup, newRecommendation)

            // 알림 false 설정 시, Reco DB 에서 해당 data 를 삭제하는 코드
            if (!newRecommendation) {
                dataRecoDeleteByNumber(preContactBase.number)
            } else {
                updateDialogDone()
            }
        }
    }

    // GroupFragment 에서 그룹명 변경에 사용되는 함수
    fun groupNameEdit(preGroupName: String, newGroupName: String) {
        viewModelScope.launch {
            val preGroupList = dataGroup.getGroupList(preGroupName)
            for (each in preGroupList) {
                val newGroupList = GroupList(
                    each.name,
                    each.number,
                    newGroupName,
                    each.image,
                    each.recentContact,
                    each.recentContactCallTime,
                    each.recommendation,
                    each.id
                )
                updateInGroupList(newGroupList)
            }
            val preContactBase = database.getContactBaseByGroup(preGroupName)
            for (each in preContactBase) {
                val newContactBase = ContactBase(
                    each.name,
                    each.number,
                    newGroupName,
                    each.image,
                    each.id
                )
                updateInContactBase(newContactBase)
            }
            groupNameEditFinished()
        }
    }

    private fun updateInGroupList(newGroupList: GroupList) {
        viewModelScope.launch {
            dataGroup.update(newGroupList)
        }
    }

    fun groupNameEditFinished() {
        _groupNameEditDone.value = true
    }

    fun groupNameEditReset() {
        _groupNameEditDone.value = false
    }


    // GroupList Adapter 에 들어갈 Data 를 가공하는 함수 (미리 정렬한 리스트를 가져와야 함)
    fun makeGroupItem(groupList: List<GroupList>) {
        viewModelScope.launch {
            val baseList = groupList
            // 해당 group 에 통화시간과 통화횟수를 추출하는 코드
            val dataList = mutableListOf<GroupTopData>()
            for (item in baseList) {
                val name = item.name
                val number = item.number
                var duration = 0L
                var times = 0
                val callLogDurationList: List<String> = dataCall.getGroupDetailList(number)
                // 만약 통화기록이 없다면(duration 이 없다면) continue
                if (callLogDurationList == emptyList<String>()) {
                    continue
                }
                for (callDuration in callLogDurationList) {
                    duration += callDuration.toLong()
                    times += 1
                }
                val addList = GroupTopData(name, number, duration, times)
                dataList.add(addList)
            }

            // dataList 중 Top 3 선정 코드
            val topList = mutableListOf<GroupTopData>()
            dataList.sortByDescending { it.duration }
            when (dataList.size) {
                0 -> {}
                1 -> topList.add(dataList[0])
                2 -> {
                    topList.add(dataList[0])
                    topList.add(dataList[1])
                }
                else -> {
                    topList.add(dataList[0])
                    topList.add(dataList[1])
                    topList.add(dataList[2])
                }
            }

            dataList.sortByDescending { it.times }
            when (dataList.size) {
                0 -> {}
                1 -> topList.add(dataList[0])
                2 -> {
                    topList.add(dataList[0])
                    topList.add(dataList[1])
                }
                else -> {
                    topList.add(dataList[0])
                    topList.add(dataList[1])
                    topList.add(dataList[2])
                }
            }

            // 결과를 리턴할 리스트
            val result = arrayListOf<GroupItem>()
            // 1. 헤더값 add
            result.add(GroupItem.Header(topList))
            // 2. 나머지 groupList 값 add
            baseList.forEach { groupList ->
                result.add(GroupItem.Item(groupList))
            }
            _groupListRecyclerView.postValue(result)
        }
    }

    // 그룹 중요도 순으로 정렬하여 반환
    fun sortGroupByImportance(it: List<GroupData>) {
        val newGroupList: List<GroupData> = it.sortedByDescending { it.importanceRating }
        _groupFragmentList.postValue(newGroupList)
    }

    // 그룹 인원 순으로 정렬하여 반환
    fun sortGroupByMembers(it: List<GroupData>) {
        val newGroupList: List<GroupData> = it.sortedByDescending { it.groupNumber }
        _groupFragmentList.postValue(newGroupList)
    }


}