package com.dreamreco.howru.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dreamreco.howru.data.entity.GroupList
import com.dreamreco.howru.list.GetAllDataFromGroupListClass
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT number,`group` FROM group_table")
    suspend fun getAllDataFromGroupList(): List<GetAllDataFromGroupListClass>

    @Query("SELECT number FROM group_table")
    suspend fun getNumberFromGroupList(): List<String>

    @Query("SELECT `group` FROM group_table ORDER BY id ASC")
    suspend fun getGroupName(): List<String>

    // 번호를 받아, 해당 groupList 를 반환하는 함수
    @Query("SELECT * FROM group_table WHERE `number` = :number ORDER BY name ASC LIMIT 1")
    suspend fun getGroupByNumber(number: String): GroupList?

    // 번호를 받아, 해당 groupList 를 삭제하는 함수
    @Query("DELETE FROM group_table WHERE `number` = :number")
    suspend fun deleteGroupByNumber(number: String)

    // 그룹명을 인자로 받아, 해당 그룹 리스트를 출력하는 함수
    @Query("SELECT * FROM group_table WHERE `group` =:group")
    suspend fun getGroupList(group: String): List<GroupList>

    // 그룹명을 인자로 받아, 해당 그룹 중 recommendation 이 true 인 인자를 리스트로 출력하는 함수
    @Query("SELECT number FROM group_table WHERE `group` =:group AND recommendation =:key")
    suspend fun getGroupListRecommendationTrue(group: String, key: Boolean = true): List<String>

    // Recommendation = true 인 GroupList 를 출력하는 함수
    @Query("SELECT * FROM group_table WHERE recommendation is :key")
    suspend fun getRecommendationGroupList(key:Boolean = true): List<GroupList>



    // 그룹명을 인자로 받아, 해당 그룹 리스트를 Live 로 출력하는 함수
    @Query("SELECT * FROM group_table WHERE `group` =:group")
    fun getGroupListLive(group: String): LiveData<List<GroupList>>

    @Query("SELECT `group` FROM group_table ORDER BY id ASC")
    fun getGroupNameFlow(): Flow<List<String>>

    @Query("SELECT * FROM group_table WHERE name LIKE :searchQuery OR number LIKE :searchQuery OR `group` LIKE :searchQuery ")
    fun searchDatabase(searchQuery: String): Flow<List<GroupList>>

}