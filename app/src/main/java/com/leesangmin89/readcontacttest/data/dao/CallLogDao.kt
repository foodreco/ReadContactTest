package com.leesangmin89.readcontacttest.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.recommendationLogic.CallLogDataForTendency
import com.leesangmin89.readcontacttest.recommendationLogic.CallLogDataForTendencyMinimal
import com.leesangmin89.readcontacttest.recommendationLogic.CallLogDataForTendencyMinimal2
import kotlinx.coroutines.flow.Flow

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
    @Query("SELECT duration, callType FROM call_history WHERE number LIKE :number ORDER BY date DESC")
    suspend fun getCallLogDataByNumber(number: String): List<CallLogDataForTendencyMinimal>

    // 전화번호를 인자로 넘겨받아, 해당 CallLog 정보를 다 가져오는 함수
    @Query("SELECT duration, date FROM call_history WHERE number LIKE :number ORDER BY date DESC")
    suspend fun getCallLogDataByNumber2(number: String): List<CallLogDataForTendencyMinimal2>

    // CallLog 정보를 다 가져오는 함수 (최신일자부터)
    @Query("SELECT number,duration,callType FROM call_history ORDER BY date DESC")
    suspend fun getAllCallLogData(): List<CallLogDataForTendency>

    // 특정 number 의 CallLog 특정 정보만 불러오는 함수 (최신일자부터)
    @Query("SELECT duration FROM call_history WHERE number LIKE :number ORDER BY date DESC")
    suspend fun getGroupDetailList(number: String): List<String>

    // 통화 기록 중, number, date, duration 이 동일한 기록을 하나 반환하는 함수
    @Query("SELECT * FROM call_history WHERE number LIKE :number AND date LIKE :date AND duration LIKE :duration LIMIT 1")
    suspend fun confirmAndInsert(number: String, date: String, duration: String): CallLogData?


    @Query("SELECT * FROM call_history ORDER BY date DESC")
    fun getAllDataByDate(): LiveData<List<CallLogData>>

    // callLogData 중 번호만 다 가져오는 함수
    @Query("SELECT number FROM call_history ORDER BY name ASC")
    fun getAllCallLogDataFlow(): Flow<List<String>>

    // 전화번호를 인자로 넘겨받아, 해당 CallLogData 를 Live 로 받아오는 함수
    @Query("SELECT * FROM call_history WHERE number LIKE :number ORDER BY date DESC")
    fun findAndReturnLive(number: String): LiveData<List<CallLogData>>

    // 모든 callLogData 를 불러오는 함수 (정렬)
    @Query("SELECT * FROM call_history ORDER BY date DESC")
    fun sortByNormal(): Flow<List<CallLogData>>

    // 연락처가 저장된 callLogData 만 불러오는 함수 (정렬)
    @Query("SELECT * FROM call_history WHERE name NOT LIKE number ORDER BY date DESC")
    fun sortByContact(): Flow<List<CallLogData>>

    // 중요 표시된 callLogData 만 불러오는 함수 (정렬)
    @Query("SELECT * FROM call_history WHERE importance LIKE :importance ORDER BY date DESC")
    fun sortByImportance(importance: Boolean = true): Flow<List<CallLogData>>

    // 특정 number 의 통화기록만 불러오는 함수 (정렬)
    @Query("SELECT * FROM call_history WHERE number LIKE :number ORDER BY date DESC")
    fun groupDetailList(number: String): Flow<List<CallLogData>>

    // 특정 number 의 통화기록 중 중요 기록만 불러오는 함수 (정렬)
    @Query("SELECT * FROM call_history WHERE number LIKE :number AND importance LIKE :importance ORDER BY date DESC")
    fun groupDetailImportanceList(
        number: String,
        importance: Boolean = true
    ): Flow<List<CallLogData>>


}