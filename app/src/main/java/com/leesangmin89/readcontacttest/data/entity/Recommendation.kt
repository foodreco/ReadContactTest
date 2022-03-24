package com.leesangmin89.readcontacttest.data.entity

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "recommendation_table")
data class Recommendation(
    var name: String,
    var number: String,
    var group: String,

    // 최근 통화 일자
    // 단위 : milliseconds
    var recentContact: String?,

    // 총 통화시간
    // 단위 : seconds
    var totalCallTime: String?,

    // 총 통화횟수
    var numberOfCalling: String?,

    // 평균 통화 시간
    // 단위 : seconds
    var avgCallTime : String,

    // 통화 빈도
    // 단위 : milliseconds
    var frequency: String?,

    var numberOfCallingBelow: Boolean = false,
    var recentCallExcess: Boolean = false,
    var frequencyExcess: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) : Parcelable