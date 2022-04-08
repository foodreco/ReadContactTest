package com.leesangmin89.readcontacttest.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import com.leesangmin89.readcontacttest.data.entity.GroupList
import com.leesangmin89.readcontacttest.data.entity.Recommendation
import com.leesangmin89.readcontacttest.main.RecommendationMinimal
import kotlinx.coroutines.flow.Flow


@Dao
interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recommendation: Recommendation)

    @Delete
    suspend fun delete(recommendation: Recommendation)

    @Update
    suspend fun update(recommendation: Recommendation)

    // Reco DB 중, number 이 동일한 기록을 하나 반환하는 함수
    @Query("SELECT id FROM recommendation_table WHERE number LIKE :number LIMIT 1")
    suspend fun confirm(number: String) : Int?

    //Reco DB 중, group 을 인자로 받아 해당 데이터를 삭제하는 함수
    @Query("DELETE FROM recommendation_table WHERE `group` LIKE :group")
    suspend fun deleteByGroup(group: String)

    //Reco DB 중, number 을 인자로 받아 해당 데이터를 삭제하는 함수
    @Query("DELETE FROM recommendation_table WHERE `number` LIKE :number")
    suspend fun deleteByNumber(number: String)

    // 알람설정된 모든 리스트를 가져오는 함수
    @Query("SELECT number FROM recommendation_table")
    suspend fun getAllNumbers(): List<String>

    // Recommendation DB 의 추천 항목 중 어느 하나가 true 인 것 중 번호를 리스트로 반환하는 함수
    @Query("SELECT number FROM recommendation_table WHERE numberOfCallingBelow LIKE :key OR recentCallExcess LIKE :key OR frequencyExcess LIKE :key")
    suspend fun getNumberDataByRecommended(key: Boolean = true): List<String>



    @Query("SELECT * FROM recommendation_table ORDER BY id ASC")
    fun getAllDataByIdASC(): LiveData<List<Recommendation>>

    // Recommendation DB 의 추천 항목 중 어느 하나가 true 인 것을 Live 로 반환하는 함수
    @Query("SELECT * FROM recommendation_table WHERE numberOfCallingBelow LIKE :key OR recentCallExcess LIKE :key OR frequencyExcess LIKE :key")
    fun getAllDataByRecommended(key: Boolean = true): Flow<List<Recommendation>>

    // Recommendation DB 중 name 과 group 만 전체 Live 로 불러오는 함수
    @Query("SELECT name,`group` FROM recommendation_table ORDER BY `group` DESC")
    fun getNameAndGroup(): Flow<List<RecommendationMinimal>>

}