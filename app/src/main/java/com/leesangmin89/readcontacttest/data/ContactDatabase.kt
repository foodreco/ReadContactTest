package com.leesangmin89.readcontacttest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.leesangmin89.readcontacttest.data.ContactBase
import com.leesangmin89.readcontacttest.data.Converters


@Database(entities = [ContactBase::class, ContactInfo::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ContactDatabase : RoomDatabase() {

//    abstract fun contactDao() : ContactDao
    abstract val contactDao : ContactDao
    abstract val contactInfoDao : ContactInfoDao

    companion object {
        @Volatile
        private var INSTANCE: ContactDatabase? = null

        fun getInstance(context: Context): ContactDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ContactDatabase::class.java,
                        "contact_database"
                    )
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

}