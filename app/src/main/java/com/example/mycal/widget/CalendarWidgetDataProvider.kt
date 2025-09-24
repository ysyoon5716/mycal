package com.example.mycal.widget

import android.content.Context
import android.util.Log
import com.example.mycal.data.local.database.CalendarDatabase
import com.example.mycal.widget.state.CalendarWidgetState
import com.example.mycal.widget.state.WidgetEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

class CalendarWidgetDataProvider(private val context: Context) {

    companion object {
        private const val TAG = "WidgetDataProvider"
    }

    private val database: CalendarDatabase by lazy {
        CalendarDatabase.getInstance(context)
    }

    suspend fun getWidgetState(): CalendarWidgetState = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching widget state")

            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault())
            val endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault())

            val startMillis = startOfDay.toInstant().toEpochMilli()
            val endMillis = endOfDay.toInstant().toEpochMilli()

            Log.d(TAG, "Fetching events from $startMillis to $endMillis")

            // Get today's events
            val todayEventsEntities = database.eventDao()
                .getEventsInRange(startMillis, endMillis)
                .first()

            Log.d(TAG, "Found ${todayEventsEntities.size} events for today")

            val todayEvents = todayEventsEntities.map { entity ->
                WidgetEvent(
                    id = entity.id,
                    title = entity.title,
                    startTimeMillis = entity.startTime,
                    endTimeMillis = entity.endTime,
                    isAllDay = entity.isAllDay,
                    color = entity.color,
                    location = entity.location
                )
            }.sortedBy { it.startTimeMillis }

            // Get week events for large widget
            val weekEvents = getWeekEvents(today)

            CalendarWidgetState(
                selectedDateMillis = System.currentTimeMillis(),
                todayEvents = todayEvents,
                weekEvents = weekEvents,
                lastUpdateTimeMillis = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching widget state", e)
            CalendarWidgetState()
        }
    }

    private suspend fun getWeekEvents(startDate: LocalDate): Map<String, List<WidgetEvent>> {
        val weekEvents = mutableMapOf<String, List<WidgetEvent>>()

        try {
            for (i in 0..6) {
                val date = startDate.plusDays(i.toLong())
                val dayStart = date.atStartOfDay(ZoneId.systemDefault())
                val dayEnd = date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault())

                val events = database.eventDao()
                    .getEventsInRange(
                        dayStart.toInstant().toEpochMilli(),
                        dayEnd.toInstant().toEpochMilli()
                    )
                    .first()
                    .map { entity ->
                        WidgetEvent(
                            id = entity.id,
                            title = entity.title,
                            startTimeMillis = entity.startTime,
                            endTimeMillis = entity.endTime,
                            isAllDay = entity.isAllDay,
                            color = entity.color,
                            location = entity.location
                        )
                    }

                weekEvents[date.toString()] = events
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching week events", e)
        }

        return weekEvents
    }

    suspend fun refreshWidget() {
        try {
            Log.d(TAG, "Refreshing widget")
            CalendarWidget().updateAllWidgets(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing widget", e)
        }
    }
}