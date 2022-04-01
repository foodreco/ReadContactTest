package com.leesangmin89.readcontacttest.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.leesangmin89.readcontacttest.data.entity.ContactInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactInfoDao {

    @Insert
    suspend fun insert(contactInfo: ContactInfo)

    @Delete
    suspend fun delete(contactInfo: ContactInfo)

    @Query("DELETE FROM info_table")
    suspend fun clear()

    @Query("SELECT * FROM info_table ORDER BY id DESC LIMIT 1")
    fun getRecentData() : LiveData<ContactInfo>

    @Query("SELECT * FROM info_table ORDER BY id DESC LIMIT 1")
    fun getRecentDataFlow() : Flow<ContactInfo>

}