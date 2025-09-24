package com.example.mycal.widget.state

import kotlinx.serialization.Serializable

@Serializable
data class CalendarWidgetState(
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val todayEvents: List<WidgetEvent> = emptyList(),
    val weekEvents: Map<String, List<WidgetEvent>> = emptyMap(),
    val monthEvents: Map<String, List<WidgetEvent>> = emptyMap(), // Added for full month view
    val viewMode: WidgetViewMode = WidgetViewMode.TODAY,
    val lastUpdateTimeMillis: Long = System.currentTimeMillis()
)

@Serializable
data class WidgetEvent(
    val id: String,
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isAllDay: Boolean,
    val color: Int,
    val location: String? = null
)

@Serializable
enum class WidgetViewMode {
    TODAY,
    WEEK,
    MONTH
}