package com.example.mycal.presentation.screens.calendar

import com.example.mycal.domain.model.CalendarDate
import com.example.mycal.domain.model.CalendarEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarDates: List<CalendarDate> = emptyList(),
    val events: List<CalendarEvent> = emptyList(),
    val monthDataMap: Map<YearMonth, List<CalendarDate>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)