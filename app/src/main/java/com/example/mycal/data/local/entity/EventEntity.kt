package com.example.mycal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = CalendarSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sourceId"])]
)
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