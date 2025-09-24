package com.example.mycal.di

import android.content.Context
import androidx.room.Room
import com.example.mycal.data.local.dao.CalendarSourceDao
import com.example.mycal.data.local.dao.EventDao
import com.example.mycal.data.local.database.CalendarDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCalendarDatabase(
        @ApplicationContext context: Context
    ): CalendarDatabase {
        return Room.databaseBuilder(
            context,
            CalendarDatabase::class.java,
            "calendar_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: CalendarDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideCalendarSourceDao(database: CalendarDatabase): CalendarSourceDao {
        return database.calendarSourceDao()
    }
}