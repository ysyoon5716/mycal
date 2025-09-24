package com.example.mycal.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.mycal.MainActivity
import com.example.mycal.widget.state.CalendarWidgetState
import com.example.mycal.widget.state.WidgetEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun ExtraLargeWidgetContent(state: CalendarWidgetState) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(8.dp)
    ) {
        // Header with month and year
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentMonth.format(monthFormatter),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Calendar grid
        FullMonthCalendarGrid(
            currentMonth = currentMonth,
            today = today,
            state = state
        )
    }
}

@Composable
private fun FullMonthCalendarGrid(
    currentMonth: YearMonth,
    today: LocalDate,
    state: CalendarWidgetState
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val previousMonth = currentMonth.minusMonths(1)
    val daysInPreviousMonth = previousMonth.lengthOfMonth()

    Column(
        modifier = GlanceModifier.fillMaxWidth()
    ) {
        // Days of week header
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            daysOfWeek.forEachIndexed { index, day ->
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = TextStyle(
                            color = when (index) {
                                0 -> GlanceTheme.colors.error // Sunday in red
                                6 -> GlanceTheme.colors.primary // Saturday in blue
                                else -> GlanceTheme.colors.onSurfaceVariant
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Calendar grid - 6 weeks to ensure all dates are shown
        var dayCounter = 1
        var previousMonthDayCounter = daysInPreviousMonth - firstDayOfWeek + 1
        var nextMonthDayCounter = 1

        for (week in 0..5) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (dayOfWeek in 0..6) {
                    when {
                        // Previous month days
                        week == 0 && dayOfWeek < firstDayOfWeek -> {
                            val date = previousMonth.atDay(previousMonthDayCounter)
                            DayCell(
                                date = date,
                                dayNumber = previousMonthDayCounter,
                                isCurrentMonth = false,
                                isToday = false,
                                events = getEventsForDate(date, state),
                                dayOfWeek = dayOfWeek
                            )
                            previousMonthDayCounter++
                        }
                        // Current month days
                        dayCounter <= daysInMonth -> {
                            val date = currentMonth.atDay(dayCounter)
                            val isToday = dayCounter == today.dayOfMonth &&
                                    currentMonth.month == today.month &&
                                    currentMonth.year == today.year

                            DayCell(
                                date = date,
                                dayNumber = dayCounter,
                                isCurrentMonth = true,
                                isToday = isToday,
                                events = getEventsForDate(date, state),
                                dayOfWeek = dayOfWeek
                            )
                            dayCounter++
                        }
                        // Next month days
                        else -> {
                            val nextMonth = currentMonth.plusMonths(1)
                            val date = nextMonth.atDay(nextMonthDayCounter)
                            DayCell(
                                date = date,
                                dayNumber = nextMonthDayCounter,
                                isCurrentMonth = false,
                                isToday = false,
                                events = getEventsForDate(date, state),
                                dayOfWeek = dayOfWeek
                            )
                            nextMonthDayCounter++
                        }
                    }
                }
            }

            // Stop if we've filled all necessary weeks
            if (week > 0 && dayCounter > daysInMonth && nextMonthDayCounter > 7) {
                break
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    dayNumber: Int,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    events: List<WidgetEvent>,
    dayOfWeek: Int
) {
    val dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val clickAction = actionStartActivity(
        Intent(androidx.glance.LocalContext.current, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("selected_date", dateMillis)
        }
    )

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(0.5.dp)
            .background(
                if (isToday) {
                    GlanceTheme.colors.primary
                } else {
                    androidx.glance.unit.ColorProvider(androidx.compose.ui.graphics.Color.Transparent)
                }
            )
            .clickable(onClick = clickAction),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(vertical = 2.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Date number at top
            Text(
                text = dayNumber.toString(),
                style = TextStyle(
                    color = when {
                        isToday -> GlanceTheme.colors.onPrimary
                        !isCurrentMonth -> GlanceTheme.colors.onSurfaceVariant
                        dayOfWeek == 0 -> GlanceTheme.colors.error // Sunday
                        dayOfWeek == 6 -> GlanceTheme.colors.primary // Saturday
                        else -> GlanceTheme.colors.onSurface
                    },
                    fontSize = 14.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            )

            // Events section below date
            if (events.isNotEmpty() && isCurrentMonth) {
                Spacer(modifier = GlanceModifier.height(1.dp))

                Column(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Show up to 3 events
                    events.take(3).forEach { event ->
                        val truncatedTitle = if (event.title.length > 8) {
                            event.title.take(8) + "..."
                        } else {
                            event.title
                        }

                        Text(
                            text = truncatedTitle,
                            style = TextStyle(
                                color = if (isToday) {
                                    GlanceTheme.colors.onPrimary
                                } else {
                                    GlanceTheme.colors.onSurfaceVariant
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    // Show indicator for more events
                    if (events.size > 3) {
                        Text(
                            text = "+${events.size - 3}",
                            style = TextStyle(
                                color = if (isToday) {
                                    GlanceTheme.colors.onPrimary
                                } else {
                                    GlanceTheme.colors.primary
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else if (events.isNotEmpty() && !isCurrentMonth) {
                // For non-current month days, show just a small dot
                Spacer(modifier = GlanceModifier.height(2.dp))
                Box(
                    modifier = GlanceModifier
                        .size(3.dp)
                        .background(GlanceTheme.colors.onSurfaceVariant)
                ) {}
            }
        }
    }
}

private fun getEventsForDate(date: LocalDate, state: CalendarWidgetState): List<WidgetEvent> {
    val dateKey = date.toString()

    // First check monthEvents (most comprehensive)
    state.monthEvents[dateKey]?.let { events ->
        return events
    }

    // Fall back to weekEvents
    state.weekEvents[dateKey]?.let { events ->
        return events
    }

    // Check if it's today and use todayEvents
    if (date == LocalDate.now()) {
        return state.todayEvents
    }

    return emptyList()
}