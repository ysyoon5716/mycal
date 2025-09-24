package com.example.mycal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val startTime: Long, // Millis since epoch
    val endTime: Long, // Millis since epoch
    val isAllDay: Boolean,
    val location: String?,
    val color: Int,
    val sourceId: String?,
    val recurrenceRule: String?
)