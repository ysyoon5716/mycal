package com.example.mycal.data.repository

import android.util.Log
import com.example.mycal.data.local.dao.EventDao
import com.example.mycal.data.mapper.EventMapper
import com.example.mycal.domain.model.CalendarEvent
import com.example.mycal.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.threeten.bp.*
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : CalendarRepository {

    companion object {
        private const val TAG = "CalendarRepository"
    }

    override fun getEventsForMonth(year: Int, month: Int): Flow<List<CalendarEvent>> {
        val startOfMonth = LocalDate.of(year, month, 1)
        val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)

        Log.d(TAG, "Getting events for month: $year-$month (from $startOfMonth to $endOfMonth)")
        return getEventsInRange(startOfMonth, endOfMonth)
    }

    override fun getEventsForWeek(startDate: LocalDate): Flow<List<CalendarEvent>> {
        val endDate = startDate.plusDays(6)
        return getEventsInRange(startDate, endDate)
    }

    override fun getEventsForDay(date: LocalDate): Flow<List<CalendarEvent>> {
        return getEventsInRange(date, date)
    }

    override fun getEventsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarEvent>> {
        val startTime = startDate.atTime(LocalTime.MIN)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val endTime = endDate.atTime(LocalTime.MAX)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        Log.d(TAG, "Query range: $startTime to $endTime (${LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault())} to ${LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault())})")

        return eventDao.getEventsInRange(startTime, endTime)
            .map { entities ->
                Log.d(TAG, "Found ${entities.size} events in database")
                entities.forEach { entity ->
                    val startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(entity.startTime), ZoneId.systemDefault())
                    val endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(entity.endTime), ZoneId.systemDefault())
                    Log.d(TAG, "Event: ${entity.title}, Start: $startDateTime, End: $endDateTime, Source: ${entity.sourceId}")
                }
                entities.map { EventMapper.toDomain(it) }
            }
    }

    override suspend fun insertEvent(event: CalendarEvent) {
        eventDao.insertEvent(EventMapper.toEntity(event))
    }

    override suspend fun updateEvent(event: CalendarEvent) {
        eventDao.updateEvent(EventMapper.toEntity(event))
    }

    override suspend fun deleteEvent(eventId: String) {
        eventDao.deleteEventById(eventId)
    }

    override suspend fun getEventById(eventId: String): CalendarEvent? {
        return eventDao.getEventById(eventId)?.let { EventMapper.toDomain(it) }
    }
}