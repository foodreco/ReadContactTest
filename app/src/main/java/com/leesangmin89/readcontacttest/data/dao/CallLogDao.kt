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

    // 전화번호를 인자로 넘겨받아, 가장 최근 통화일자, 시간, 유형을 받아오는 함수
    @Query("SELECT * FROM call_history WHERE number LIKE :number ORDER BY date DESC LIMIT 1")
    suspend fun getDDC(number: String): CallLogData?

    // 전화번호를 인자로 넘겨받아, 해당 CallLog 정보를 다 가져오는 함수
    @Query("SELECT * FROM call_history WHERE number LIKE :number ORDER BY date DESC")
    suspend fun getCallLogDataByNumber(number: String): List<CallLogData>

    // CallLog 정보를 다 가져오는 함수 (최신일자부터)
    @Query("SELECT * FROM call_history ORDER BY date DESC")
    suspend fun getAllCallLogData(): List<CallLogData>

    // 통화 기록 중, number, date, duration 이 동일한 기록을 하나 반환하는 함수
    @Query("SELECT * FROM call_history WHERE number LIKE :number AND date LIKE :date AND duration LIKE :duration LIMIT 1")
    suspend fun confirmAndInsert(number: String, date:String, duration:String) : CallLogData?



    @Query("SELECT * FROM call_history ORDER BY date DESC")
    fun getAllDataByDate(): LiveData<List<CallLogData>>

    // 전화번호를 인자로 넘겨받아, 해당 CallLogData 를 Live 로 받아오는 함수
    @Query("SELECT * FROM call_history WHERE number LIKE :number ORDER BY date DESC")
    fun findAndReturnLive(number: String): LiveData<List<CallLogData>>



}