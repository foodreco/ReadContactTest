package com.leesangmin89.readcontacttest.data.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.leesangmin89.readcontacttest.data.entity.ContactBase
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactBase)

    @Delete
    suspend fun delete(contact: ContactBase)

    @Update
    suspend fun update(contact: ContactBase)

    @Query("DELETE FROM contact_table")
    suspend fun clear()

    @Query("SELECT `group` FROM contact_table WHERE `group`is not '' ")
    suspend fun getGroupName(): List<String>

    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    suspend fun getAllContactBaseList(): List<ContactBase>

    @Query("SELECT * FROM contact_table WHERE `group` =:key")
    suspend fun getGroupList(key: String): List<ContactBase>

    // 번호를 받아서, 해당 ContactBase 를 불러오는 함수
    @Query("SELECT * FROM contact_table WHERE `number` =:number LIMIT 1")
    suspend fun getContact(number: String): ContactBase

    @Query("SELECT name FROM contact_table WHERE `number` =:key")
    suspend fun getName(key: String): String

    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    suspend fun getAllDataByNameASCTest(): List<ContactBase>

    @Query("SELECT * FROM contact_table ORDER BY name DESC")
    suspend fun getAllDataByNameDESCTest(): List<ContactBase>

    @Query("SELECT * FROM contact_table ORDER BY number ASC")
    suspend fun getAllDataByNumberASCTest(): List<ContactBase>



    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    fun getAllDataByNameASC(): LiveData<List<ContactBase>>

    @Query("SELECT * FROM contact_table ORDER BY name DESC")
    fun getAllDataByNameDESC(): LiveData<List<ContactBase>>

    @Query("SELECT * FROM contact_table ORDER BY number ASC")
    fun getAllDataByNumberASC(): LiveData<List<ContactBase>>

    @Query("SELECT * FROM contact_table WHERE `group` is not :argsGroup ORDER BY name ASC")
    fun getDataExceptArgsGroup(argsGroup: String): LiveData<List<ContactBase>>

    @Query("SELECT * FROM contact_table WHERE name LIKE :searchQuery OR number LIKE :searchQuery OR `group` LIKE :searchQuery ")
    fun searchDatabase(searchQuery: String): Flow<List<ContactBase>>

}