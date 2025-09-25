package com.example.mycal.widget.state

import com.example.mycal.widget.model.WidgetCalendarDate
import kotlinx.serialization.Serializable

@Serializable
data class CalendarWidgetState(
    val year: Int,
    val month: Int,
    val monthName: String,
    val calendarDates: List<WidgetCalendarDate> = emptyList(),
    val isLoading: Boolean = false,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    companion object {
        fun default(): CalendarWidgetState {
            val now = org.threeten.bp.LocalDate.now()
            return CalendarWidgetState(
                year = now.year,
                month = now.monthValue,
                monthName = now.month.name,
                calendarDates = emptyList(),
                isLoading = true
            )
        }
    }
}