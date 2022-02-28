package com.leesangmin89.readcontacttest.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.data.entity.Recommendation


@Dao
interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recommendation: Recommendation)

    @Delete
    suspend fun delete(recommendation: Recommendation)

    @Update
    suspend fun update(recommendation: Recommendation)

    // Reco DB 중, number 이 동일한 기록을 하나 반환하는 함수
    @Query("SELECT * FROM recommendation_table WHERE number LIKE :number LIMIT 1")
    suspend fun confirm(number: String) : Recommendation?

    //Reco DB 중, group 을 인자로 받아 해당 데이터를 삭제하는 함수
    @Query("DELETE FROM recommendation_table WHERE `group` LIKE :group")
    suspend fun deleteByGroup(group: String)

    //Reco DB 중, number 을 인자로 받아 해당 데이터를 삭제하는 함수
    @Query("DELETE FROM recommendation_table WHERE `number` LIKE :number")
    suspend fun deleteByNumber(number: String)






    @Query("SELECT * FROM recommendation_table ORDER BY name ASC")
    fun getAllDataByNameASC(): LiveData<List<Recommendation>>
}