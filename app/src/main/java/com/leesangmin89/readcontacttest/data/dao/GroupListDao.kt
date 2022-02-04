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

    @Update
    suspend fun update(groupList: GroupList)

    @Query("DELETE FROM group_table")
    suspend fun clear()

    @Query("SELECT * FROM group_table ORDER BY name ASC")
    fun getAllDataByNameASC(): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table ORDER BY name DESC")
    fun getAllDataByNameDESC(): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table ORDER BY number ASC")
    fun getAllDataByNumberASC(): LiveData<List<GroupList>>

    @Query("SELECT * FROM group_table WHERE `number` =:number")
    suspend fun getGroupByNumber(number:String) : GroupList

    @Query("SELECT * FROM group_table WHERE `group` =:key")
    suspend fun getGroupList(key:String) : List<GroupList>

    @Query("SELECT * FROM group_table WHERE name LIKE :searchQuery OR number LIKE :searchQuery OR `group` LIKE :searchQuery ")
    fun searchDatabase(searchQuery: String) : kotlinx.coroutines.flow.Flow<List<GroupList>>
}