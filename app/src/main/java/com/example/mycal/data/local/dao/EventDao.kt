package com.example.mycal.data.local.dao

import androidx.room.*
import com.example.mycal.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT COUNT(*) FROM events")
    suspend fun getTotalEventCount(): Int

    @Query("SELECT COUNT(*) FROM events WHERE sourceId = :sourceId")
    suspend fun getEventCountBySource(sourceId: String): Int

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<EventEntity>
    @Query("SELECT * FROM events WHERE startTime <= :endTime AND endTime >= :startTime ORDER BY startTime ASC")
    fun getEventsInRange(startTime: Long, endTime: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String)

    @Query("DELETE FROM events WHERE sourceId = :sourceId")
    suspend fun deleteEventsBySource(sourceId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
}