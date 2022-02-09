package com.leesangmin89.readcontacttest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "info_table")
data class ContactInfo(
    val contactNumber : Int? = 0,
    val activatedContact : Int? = 0,
    val mostRecentContact : String? = "",
    val mostContactName : String? = "",
    val mostContactTimes : Int? = 0,
    @PrimaryKey(autoGenerate = true) val id : Int = 0
)

