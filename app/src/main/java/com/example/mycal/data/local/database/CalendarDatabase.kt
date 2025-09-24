package com.example.mycal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
}