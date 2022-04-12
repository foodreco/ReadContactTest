package com.dreamreco.howru.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dreamreco.howru.data.dao.*
import com.dreamreco.howru.data.entity.*


@Database(entities = [ContactBase::class, GroupList::class, CallLogData::class, Recommendation::class, Tendency::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ContactDatabase : RoomDatabase() {

    abstract val contactDao : ContactDao
    abstract val groupListDao : GroupListDao
    abstract val callLogDao : CallLogDao
    abstract val recommendationDao : RecommendationDao
    abstract val tendencyDao : TendencyDao


}