package com.example.mycal.data.repository

import com.example.mycal.data.local.dao.CalendarSourceDao
import com.example.mycal.data.local.entity.CalendarSourceEntity
import com.example.mycal.data.sync.CalendarSyncManager
import com.example.mycal.domain.model.CalendarSource
import com.example.mycal.domain.repository.CalendarSourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalendarSourceRepositoryImpl @Inject constructor(
    private val calendarSourceDao: CalendarSourceDao,
    private val syncManager: CalendarSyncManager
) : CalendarSourceRepository {

    override fun getAllSources(): Flow<List<CalendarSource>> {
        return calendarSourceDao.getAllSources().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override fun getActiveSources(): Flow<List<CalendarSource>> {
        return calendarSourceDao.getActiveSources().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun getSourceById(sourceId: String): CalendarSource? {
        return calendarSourceDao.getSourceById(sourceId)?.toDomain()
    }

    override suspend fun addSource(source: CalendarSource) {
        calendarSourceDao.insertSource(source.toEntity())
        syncSource(source.id)
    }

    override suspend fun updateSource(source: CalendarSource) {
        calendarSourceDao.updateSource(source.toEntity())
    }

    override suspend fun deleteSource(sourceId: String) {
        calendarSourceDao.deleteSourceById(sourceId)
        syncManager.cancelSync(sourceId)
    }

    override suspend fun setSyncEnabled(sourceId: String, enabled: Boolean) {
        calendarSourceDao.setSyncEnabled(sourceId, enabled)
        if (enabled) {
            syncSource(sourceId)
        }
    }

    override suspend fun syncSource(sourceId: String) {
        syncManager.syncNow(sourceId)
    }

    override suspend fun syncAllSources() {
        syncManager.syncNow()
    }

    private fun CalendarSourceEntity.toDomain(): CalendarSource {
        return CalendarSource(
            id = id,
            url = url,
            name = name,
            color = color,
            syncEnabled = syncEnabled,
            lastSyncTime = if (lastSyncTime == 0L) null else lastSyncTime
        )
    }

    private fun CalendarSource.toEntity(): CalendarSourceEntity {
        return CalendarSourceEntity(
            id = id,
            url = url,
            name = name,
            color = color,
            syncEnabled = syncEnabled,
            lastSyncTime = lastSyncTime ?: 0L
        )
    }
}