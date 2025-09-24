package com.example.mycal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_sources")
data class CalendarSourceEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val name: String,
    val color: Int,
    val syncEnabled: Boolean,
    val lastSyncTime: Long
)