package com.example.mycal.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mycal.data.local.dao.CalendarSourceDao
import com.example.mycal.data.local.dao.EventDao
import com.example.mycal.data.local.entity.CalendarSourceEntity
import com.example.mycal.data.local.entity.EventEntity

@Database(
    entities = [EventEntity::class, CalendarSourceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CalendarDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun calendarSourceDao(): CalendarSourceDao

    companion object {
        @Volatile
        private var INSTANCE: CalendarDatabase? = null

        fun getInstance(context: Context): CalendarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalendarDatabase::class.java,
                    "calendar_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}