package com.leesangmin89.readcontacttest.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.data.entity.Recommendation
import com.leesangmin89.readcontacttest.data.entity.Tendency
import kotlinx.coroutines.flow.Flow


@Dao
interface TendencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tendency: Tendency)

    @Delete
    suspend fun delete(tendency: Tendency)

    @Update
    suspend fun update(tendency: Tendency)

    @Query("SELECT * FROM tendency_table ORDER BY id DESC LIMIT 1")
    suspend fun getRecentTendency(): Tendency?


    @Query("SELECT * FROM tendency_table ORDER BY id DESC LIMIT 1")
    fun getRecentTendencyDataLive(): Flow<Tendency>?
}