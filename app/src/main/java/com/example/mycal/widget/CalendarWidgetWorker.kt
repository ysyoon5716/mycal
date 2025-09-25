package com.example.mycal.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mycal.domain.repository.CalendarRepository
import com.example.mycal.widget.state.CalendarWidgetStateDefinition
import com.example.mycal.widget.model.WidgetCalendarDate
import com.example.mycal.widget.model.WidgetEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.DayOfWeek
import org.threeten.bp.temporal.TemporalAdjusters

@HiltWorker
class CalendarWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val calendarRepository: CalendarRepository
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val TAG = "CalendarWidgetWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val manager = GlanceAppWidgetManager(context)
            val widgetIds = manager.getGlanceIds(CalendarAppWidget::class.java)

            if (widgetIds.isEmpty()) {
                Log.d(TAG, "No widgets found")
                return Result.success()
            }

            // Get current state from the first widget
            val currentState = widgetIds.firstOrNull()?.let { glanceId ->
                CalendarWidgetStateDefinition.getDataStore(context, "widget_$glanceId")
                    .data.first()
            } ?: run {
                val now = LocalDate.now()
                com.example.mycal.widget.state.CalendarWidgetState(
                    year = now.year,
                    month = now.monthValue,
                    monthName = now.month.name,
                    calendarDates = emptyList(),
                    isLoading = false
                )
            }

            val yearMonth = YearMonth.of(currentState.year, currentState.month)
            val widgetDates = generateWidgetCalendarDates(yearMonth)

            // Update all widgets
            widgetIds.forEach { glanceId ->
                updateAppWidgetState(
                    context = context,
                    definition = CalendarWidgetStateDefinition,
                    glanceId = glanceId
                ) { state ->
                    state.copy(
                        calendarDates = widgetDates,
                        isLoading = false,
                        lastUpdateTime = System.currentTimeMillis()
                    )
                }
            }

            CalendarAppWidget().updateAll(context)

            Log.d(TAG, "Widget updated successfully for ${yearMonth.month} ${yearMonth.year}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widget", e)
            Result.failure()
        }
    }

    private suspend fun generateWidgetCalendarDates(
        yearMonth: YearMonth
    ): List<WidgetCalendarDate> {
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        val today = LocalDate.now()

        // Get the first day to display (start of week containing first day of month)
        val firstDayOfCalendar = firstDayOfMonth.with(
            TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
        )

        // Get the last day to display (end of week containing last day of month)
        val lastDayOfCalendar = lastDayOfMonth.with(
            TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)
        )

        // Get events for the range
        val events = try {
            calendarRepository.getEventsInRange(
                firstDayOfCalendar,
                lastDayOfCalendar
            ).first()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching events", e)
            emptyList()
        }

        val dates = mutableListOf<WidgetCalendarDate>()
        var currentDate = firstDayOfCalendar

        while (!currentDate.isAfter(lastDayOfCalendar)) {
            val dayEvents = events.filter { event ->
                event.isOnDate(currentDate.atStartOfDay())
            }.map { event ->
                WidgetEvent(
                    id = event.id,
                    title = event.title,
                    time = if (!event.isAllDay) {
                        "${event.startTime.hour}:${event.startTime.minute.toString().padStart(2, '0')}"
                    } else null,
                    color = event.color,
                    isAllDay = event.isAllDay
                )
            }

            dates.add(
                WidgetCalendarDate(
                    dayOfMonth = currentDate.dayOfMonth,
                    month = currentDate.monthValue,
                    year = currentDate.year,
                    isToday = currentDate == today,
                    isCurrentMonth = currentDate.month == yearMonth.month,
                    isWeekend = currentDate.dayOfWeek == DayOfWeek.SATURDAY ||
                               currentDate.dayOfWeek == DayOfWeek.SUNDAY,
                    isSunday = currentDate.dayOfWeek == DayOfWeek.SUNDAY,
                    isSaturday = currentDate.dayOfWeek == DayOfWeek.SATURDAY,
                    events = dayEvents
                )
            )

            currentDate = currentDate.plusDays(1)
        }

        return dates
    }
}