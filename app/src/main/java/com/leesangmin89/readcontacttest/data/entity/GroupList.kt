package com.leesangmin89.readcontacttest.data.entity

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
    var image: Bitmap? = null,
    var recentContact : String?,
    var recentContactCallTime : String?,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) : Parcelable