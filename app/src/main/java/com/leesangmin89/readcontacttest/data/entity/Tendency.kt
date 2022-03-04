package com.leesangmin89.readcontacttest.data.entity

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tendency_table")
data class Tendency(

    var allCallIncoming: String = "0",
    var allCallOutgoing: String = "0",
    var allCallMissed: String = "0",
    var allCallCount: String = "0",
    var allCallDuration:String = "0",
    var allCallPartnerList : List<String>,

    var groupListCallIncoming: String = "0",
    var groupListCallOutgoing: String = "0",
    var groupListCallMissed: String = "0",
    var groupListCallCount: String = "0",
    var groupListCallDuration:String = "0",
    var groupListCallPartnerList : List<String>,

    var recommendationCallIncoming: String = "0",
    var recommendationCallOutgoing: String = "0",
    var recommendationCallMissed: String = "0",
    var recommendationCallCount: String = "0",
    var recommendationCallDuration:String = "0",
    var recommendationCallPartnerList : List<String>,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) : Parcelable