package com.leesangmin89.readcontacttest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.leesangmin89.readcontacttest.data.dao.*
import com.leesangmin89.readcontacttest.data.entity.*


@Database(entities = [ContactBase::class, ContactInfo::class, GroupList::class, CallLogData::class, Recommendation::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ContactDatabase : RoomDatabase() {

    abstract val contactDao : ContactDao
    abstract val contactInfoDao : ContactInfoDao
    abstract val groupListDao : GroupListDao
    abstract val callLogDao : CallLogDao
    abstract val recommendationDao : RecommendationDao


}