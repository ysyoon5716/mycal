package com.example.mycal.data.repository

import com.example.mycal.data.local.dao.EventDao
import com.example.mycal.data.mapper.EventMapper
import com.example.mycal.domain.model.CalendarEvent
import com.example.mycal.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : CalendarRepository {

    override fun getEventsForMonth(year: Int, month: Int): Flow<List<CalendarEvent>> {
        val startOfMonth = LocalDate.of(year, month, 1)
        val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)

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

        return eventDao.getEventsInRange(startTime, endTime)
            .map { entities ->
                entities.map { EventMapper.toDomain(it) }
            }
            .onStart {
                // Emit empty list immediately to ensure Flow starts
                emit(emptyList())
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