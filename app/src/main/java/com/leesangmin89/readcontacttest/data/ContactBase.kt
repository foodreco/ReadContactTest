package com.leesangmin89.readcontacttest.data

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "contact_table")
data class ContactBase(
    var name : String,
    var number : String,
//    var image : Bitmap?,
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
) : Parcelable