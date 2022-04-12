package com.dreamreco.howru.data.dao

import androidx.room.*
import com.dreamreco.howru.data.entity.Tendency
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