package com.example.mycal.widget.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetCalendarDate(
    val dayOfMonth: Int,
    val month: Int,
    val year: Int,
    val isToday: Boolean = false,
    val isCurrentMonth: Boolean = true,
    val isWeekend: Boolean = false,
    val isSunday: Boolean = false,
    val isSaturday: Boolean = false,
    val events: List<WidgetEvent> = emptyList()
) {
    val hasEvents: Boolean = events.isNotEmpty()

    fun getDisplayEvents(maxEvents: Int = 3): List<WidgetEvent> {
        return events.take(maxEvents)
    }

    fun hasMoreEvents(maxEvents: Int = 3): Boolean {
        return events.size > maxEvents
    }

    fun getMoreEventsCount(maxEvents: Int = 3): Int {
        return (events.size - maxEvents).coerceAtLeast(0)
    }
}