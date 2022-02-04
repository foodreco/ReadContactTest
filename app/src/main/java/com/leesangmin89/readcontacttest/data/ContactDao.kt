package com.leesangmin89.readcontacttest.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.leesangmin89.readcontacttest.data.ContactBase

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
    suspend fun getGroupName() : List<String>

    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    fun getAllDataByNameASC(): LiveData<List<ContactBase>>

    @Query("SELECT * FROM contact_table ORDER BY name DESC")
    fun getAllDataByNameDESC(): LiveData<List<ContactBase>>

    @Query("SELECT * FROM contact_table ORDER BY number ASC")
    fun getAllDataByNumberASC(): LiveData<List<ContactBase>>

    @Query("SELECT * FROM contact_table WHERE `group` =:key")
    suspend fun getGroupList(key:String) : List<ContactBase>

    @Query("SELECT * FROM contact_table WHERE name LIKE :searchQuery OR number LIKE :searchQuery OR `group` LIKE :searchQuery ")
    fun searchDatabase(searchQuery: String) : kotlinx.coroutines.flow.Flow<List<ContactBase>>



}