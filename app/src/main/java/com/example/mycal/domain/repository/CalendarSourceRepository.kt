package com.example.mycal.domain.repository

import com.example.mycal.domain.model.CalendarSource
import kotlinx.coroutines.flow.Flow

interface CalendarSourceRepository {
    fun getAllSources(): Flow<List<CalendarSource>>
    fun getActiveSources(): Flow<List<CalendarSource>>
    suspend fun getSourceById(sourceId: String): CalendarSource?
    suspend fun addSource(source: CalendarSource)
    suspend fun updateSource(source: CalendarSource)
    suspend fun deleteSource(sourceId: String)
    suspend fun setSyncEnabled(sourceId: String, enabled: Boolean)
    suspend fun syncSource(sourceId: String)
    suspend fun syncAllSources()
}