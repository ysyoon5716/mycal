package com.example.mycal.domain.model

import org.threeten.bp.LocalDate

data class CalendarDate(
    val date: LocalDate,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isCurrentMonth: Boolean = true,
    val events: List<CalendarEvent> = emptyList()
) {
    val dayOfMonth: Int = date.dayOfMonth
    val isWeekend: Boolean = date.dayOfWeek.value >= 6
    val hasEvents: Boolean = events.isNotEmpty()
}