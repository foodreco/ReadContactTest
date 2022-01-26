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

    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    fun getAllDataByNameASC(): LiveData<List<ContactBase>>

    @Query("SELECT * FROM contact_table ORDER BY name DESC")
    fun getAllDataByNameDESC(): LiveData<List<ContactBase>>

    @Query("SELECT * FROM contact_table ORDER BY number ASC")
    fun getAllDataByNumberASC(): LiveData<List<ContactBase>>

}