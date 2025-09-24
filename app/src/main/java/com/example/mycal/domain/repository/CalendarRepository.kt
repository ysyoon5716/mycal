package com.example.mycal.domain.repository

import com.example.mycal.domain.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

interface CalendarRepository {
    fun getEventsForMonth(year: Int, month: Int): Flow<List<CalendarEvent>>
    fun getEventsForWeek(startDate: LocalDate): Flow<List<CalendarEvent>>
    fun getEventsForDay(date: LocalDate): Flow<List<CalendarEvent>>
    fun getEventsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarEvent>>
    suspend fun insertEvent(event: CalendarEvent)
    suspend fun updateEvent(event: CalendarEvent)
    suspend fun deleteEvent(eventId: String)
    suspend fun getEventById(eventId: String): CalendarEvent?
}