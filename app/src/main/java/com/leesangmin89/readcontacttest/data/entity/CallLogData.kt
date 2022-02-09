package com.leesangmin89.readcontacttest.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "call_history")
data class CallLogData(
    var name: String?,
    var number: String,
    var date: String,
    var duration: String,
    var callType: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) : Parcelable