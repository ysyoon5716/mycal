package com.example.mycal.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.mycal.widget.state.CalendarWidgetState
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
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
            .padding(12.dp)
    ) {
        // Header with month and year
        Text(
            text = currentMonth.format(monthFormatter),
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Mini calendar grid
        MiniCalendarGrid(currentMonth, today, state)

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Today's events summary
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surface)
                .padding(8.dp)
        ) {
            Text(
                text = "Today's Events",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            if (state.todayEvents.isEmpty()) {
                Text(
                    text = "No events",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            } else {
                state.todayEvents.take(2).forEach { event ->
                    Text(
                        text = "• ${event.title}",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 12.sp
                        )
                    )
                }
                if (state.todayEvents.size > 2) {
                    Text(
                        text = "+${state.todayEvents.size - 2} more",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniCalendarGrid(
    currentMonth: YearMonth,
    today: LocalDate,
    state: CalendarWidgetState
) {
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()

    Column(
        modifier = GlanceModifier.fillMaxWidth()
    ) {
        // Days of week header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            daysOfWeek.forEach { day ->
                Box(
                    modifier = GlanceModifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Calendar grid
        var dayCounter = 1
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (dayOfWeek in 0..6) {
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        // Empty cell before month starts
                        Box(modifier = GlanceModifier.size(28.dp)) {}
                    } else if (dayCounter <= daysInMonth) {
                        val isToday = dayCounter == today.dayOfMonth &&
                                currentMonth.month == today.month &&
                                currentMonth.year == today.year

                        Box(
                            modifier = GlanceModifier
                                .size(28.dp)
                                .then(
                                    if (isToday) {
                                        GlanceModifier.background(GlanceTheme.colors.primary)
                                    } else {
                                        GlanceModifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayCounter.toString(),
                                style = TextStyle(
                                    color = if (isToday) {
                                        GlanceTheme.colors.onPrimary
                                    } else {
                                        GlanceTheme.colors.onSurface
                                    },
                                    fontSize = 11.sp
                                )
                            )
                        }
                        dayCounter++
                    } else {
                        // Empty cell after month ends
                        Box(modifier = GlanceModifier.size(28.dp)) {}
                    }
                }
            }
        }
    }
}