package com.leesangmin89.readcontacttest.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context : Context
    ) = Room.databaseBuilder(
        context,
        ContactDatabase::class.java,
        "contact_database"
    ).build()

    @Singleton
    @Provides
    fun provideDao(database: ContactDatabase) = database.contactDao

    @Singleton
    @Provides
    fun provideInfoDao(database: ContactDatabase) = database.contactInfoDao

}