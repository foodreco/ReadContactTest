package com.leesangmin89.readcontacttest.data.entity

import android.os.Parcelable
import android.text.Editable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "call_history")
data class CallLogData(
    var name: String?,
    var number: String,
    var date: String? = "",
    var duration: String? = "",
    var callType: String? = "",
    var callContent: String? = "",
    var callKeyword: String? = "",
    var importance : Boolean? = false,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) : Parcelable