package com.leesangmin89.readcontacttest.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.ContactBase

@Dao
interface CallLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callLogData: CallLogData)

    @Delete
    suspend fun delete(callLogData: CallLogData)

    @Update
    suspend fun update(callLogData: CallLogData)

    @Query("DELETE FROM call_history")
    suspend fun clear()

    @Query("SELECT * FROM call_history WHERE number LIKE :number")
    suspend fun findAndReturn(number: String): List<CallLogData>

    // 전화번호를 인자로 넘겨받아, 가장 최근 통화일자, 시간, 유형을 받아오는 함수
    @Query("SELECT * FROM call_history WHERE number LIKE :number ORDER BY date DESC LIMIT 1")
    suspend fun getDDC(number: String) : CallLogData?

    @Query("SELECT * FROM call_history ORDER BY date DESC")
    fun getAllDataByDate(): LiveData<List<CallLogData>>

}