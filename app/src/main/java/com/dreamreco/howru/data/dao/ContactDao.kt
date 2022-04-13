package com.dreamreco.howru.data.dao

import androidx.room.*
import com.dreamreco.howru.data.entity.ContactBase
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

    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    suspend fun getAllContactBaseList(): List<ContactBase>

    @Query("SELECT number FROM contact_table ORDER BY name ASC")
    suspend fun getNumbersContactBaseList(): List<String>

    // 번호를 받아서, 해당 ContactBase 를 불러오는 함수
    @Query("SELECT * FROM contact_table WHERE `number` =:number LIMIT 1")
    suspend fun getContact(number: String): ContactBase?

    // 그룹을 받아서, 해당 리스트를 가져오는 함수
    @Query("SELECT * FROM contact_table WHERE `group` =:group")
    suspend fun getContactBaseByGroup(group:String): List<ContactBase>


    // 특정 그룹을 제외한 group 의 ContactBase 리스트를 이름순으로 가져오는 함수
    @Query("SELECT * FROM contact_table WHERE `group` is not :argsGroup ORDER BY name ASC")
    fun getDataExceptArgsGroup(argsGroup: String): Flow<List<ContactBase>>

    @Query("SELECT * FROM contact_table WHERE name LIKE :searchQuery OR number LIKE :searchQuery OR `group` LIKE :searchQuery ORDER BY name ASC")
    fun searchDatabase(searchQuery: String): Flow<List<ContactBase>>

    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    fun getAllContactBaseFlow(): Flow<List<ContactBase>>

    @Query("SELECT number FROM contact_table ORDER BY name ASC LIMIT 1")
    fun emptyCheckContactBase(): Flow<String?>


}