package com.example.mycal.domain.model

import org.threeten.bp.LocalDateTime

data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isAllDay: Boolean = false,
    val location: String? = null,
    val color: Int = 0xFF1976D2.toInt(),
    val sourceId: String? = null,
    val recurrenceRule: String? = null
) {
    fun isOnDate(date: LocalDateTime): Boolean {
        val startDate = startTime.toLocalDate()
        val endDate = endTime.toLocalDate()
        val checkDate = date.toLocalDate()

        return checkDate in startDate..endDate
    }
}