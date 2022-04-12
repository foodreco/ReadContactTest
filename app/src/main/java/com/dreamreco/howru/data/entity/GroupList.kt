package com.dreamreco.howru.data.entity

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "group_table")
data class GroupList(
    var name: String,
    var number: String,
    var group: String,
    var image: Bitmap?,
    var recentContact : String?,
    var recentContactCallTime : String?,
    var recommendation : Boolean = false,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) : Parcelable