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
    suspend fun deleteByGroupName(group:String)

    @Update
    suspend fun update(groupList: GroupList)

    @Query("DELETE FROM group_table")
    suspend fun clear()

    @Query("SELECT `group` FROM group_table ")
    suspend fun getGroupName() : List<String>

    @Query("SELECT * FROM group_table ORDER BY name ASC")
    fun getAllDataByNameASC(): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table ORDER BY name DESC")
    fun getAllDataByNameDESC(): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table ORDER BY number ASC")
    fun getAllDataByNumberASC(): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table WHERE `number` =:number ORDER BY name ASC LIMIT 1")
    suspend fun getGroupByNumber(number:String) : GroupList

    @Query("SELECT * FROM group_table WHERE `group` =:key")
    suspend fun getGroupList(key:String) : List<GroupList>

    @Query("SELECT * FROM group_table WHERE name LIKE :searchQuery OR number LIKE :searchQuery OR `group` LIKE :searchQuery ")
    fun searchDatabase(searchQuery: String) : kotlinx.coroutines.flow.Flow<List<GroupList>>
}