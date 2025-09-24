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
import org.threeten.bp.format.TextStyle as DateTextStyle
import java.util.Locale

@Composable
fun LargeWidgetContent(state: CalendarWidgetState) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(6.dp)
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Calendar grid with events
        FullMonthCalendarGridLarge(
            currentMonth = currentMonth,
            today = today,
            state = state
        )
    }
}

@Composable
private fun FullMonthCalendarGridLarge(
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
                .padding(bottom = 3.dp),
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
                            fontSize = 12.sp,
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
                            DayCellLarge(
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

                            DayCellLarge(
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
                            DayCellLarge(
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
private fun DayCellLarge(
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
            .height(42.dp)
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
            modifier = GlanceModifier.fillMaxSize().padding(vertical = 1.dp),
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
                    fontSize = 12.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            )

            // Events section below date
            if (events.isNotEmpty() && isCurrentMonth) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Show up to 2 events for Large widget
                    events.take(2).forEach { event ->
                        val truncatedTitle = if (event.title.length > 6) {
                            event.title.take(6) + "..."
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
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    // Show indicator for more events
                    if (events.size > 2) {
                        Text(
                            text = "+${events.size - 2}",
                            style = TextStyle(
                                color = if (isToday) {
                                    GlanceTheme.colors.onPrimary
                                } else {
                                    GlanceTheme.colors.primary
                                },
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else if (events.isNotEmpty() && !isCurrentMonth) {
                // For non-current month days, show just a small dot
                Box(
                    modifier = GlanceModifier
                        .size(2.dp)
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