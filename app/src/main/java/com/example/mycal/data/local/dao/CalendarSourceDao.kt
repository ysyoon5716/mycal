package com.example.mycal.data.local.dao

import androidx.room.*
import com.example.mycal.data.local.entity.CalendarSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarSourceDao {

    @Query("SELECT * FROM calendar_sources")
    fun getAllSources(): Flow<List<CalendarSourceEntity>>

    @Query("SELECT * FROM calendar_sources WHERE syncEnabled = 1")
    fun getActiveSources(): Flow<List<CalendarSourceEntity>>

    @Query("SELECT * FROM calendar_sources WHERE id = :sourceId")
    suspend fun getSourceById(sourceId: String): CalendarSourceEntity?

    @Insert
    suspend fun insertSource(source: CalendarSourceEntity)

    @Update
    suspend fun updateSource(source: CalendarSourceEntity)

    @Delete
    suspend fun deleteSource(source: CalendarSourceEntity)

    @Query("DELETE FROM calendar_sources WHERE id = :sourceId")
    suspend fun deleteSourceById(sourceId: String)

    @Query("UPDATE calendar_sources SET lastSyncTime = :syncTime WHERE id = :sourceId")
    suspend fun updateSyncTime(sourceId: String, syncTime: Long)

    @Query("UPDATE calendar_sources SET syncEnabled = :enabled WHERE id = :sourceId")
    suspend fun setSyncEnabled(sourceId: String, enabled: Boolean)
}