package com.leesangmin89.readcontacttest.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import com.leesangmin89.readcontacttest.data.entity.GroupList

@Dao
interface GroupListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(groupList: GroupList)

    @Delete
    suspend fun delete(groupList: GroupList)

    @Query("DELETE FROM group_table WHERE `group` =:group")
    suspend fun deleteByGroupName(group: String)

    @Update
    suspend fun update(groupList: GroupList)

    @Query("DELETE FROM group_table")
    suspend fun clear()

    @Query("SELECT * FROM group_table")
    suspend fun getAllDataFromGroupList(): List<GroupList>

    @Query("SELECT `group` FROM group_table ")
    suspend fun getGroupName(): List<String>

    // 번호를 받아, 해당 groupList 를 반환하는 함수
    @Query("SELECT * FROM group_table WHERE `number` = :number ORDER BY name ASC LIMIT 1")
    suspend fun getGroupByNumber(number: String): GroupList?

    // 그룹명을 인자로 받아, 해당 그룹 리스트를 출력하는 함수
    @Query("SELECT * FROM group_table WHERE `group` =:group")
    suspend fun getGroupList(group: String): List<GroupList>

    // Recommendation = true 인 GroupList 를 출력하는 함수
    @Query("SELECT * FROM group_table WHERE recommendation is :key")
    suspend fun getRecommendationGroupList(key:Boolean = true): List<GroupList>



    // 그룹명을 인자로 받아, 해당 그룹 리스트를 Live 로 출력하는 함수
    @Query("SELECT * FROM group_table WHERE `group` =:group")
    fun getGroupListLive(group: String): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table ORDER BY name ASC")
    fun getAllDataByNameASC(): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table ORDER BY name DESC")
    fun getAllDataByNameDESC(): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table ORDER BY number ASC")
    fun getAllDataByNumberASC(): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table WHERE name LIKE :searchQuery OR number LIKE :searchQuery OR `group` LIKE :searchQuery ")
    fun searchDatabase(searchQuery: String): kotlinx.coroutines.flow.Flow<List<GroupList>>
}