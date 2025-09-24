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
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun MediumWidgetContent(state: CalendarWidgetState) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val today = org.threeten.bp.LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp)
    ) {
        // Header with date
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = today.format(dateFormatter),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Events list
        if (state.todayEvents.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No events today",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                )
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                state.todayEvents.take(3).forEach { event ->
                    EventItemRow(
                        event = event,
                        timeFormatter = timeFormatter
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }

                if (state.todayEvents.size > 3) {
                    Text(
                        text = "+${state.todayEvents.size - 3} more",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EventItemRow(
    event: com.example.mycal.widget.state.WidgetEvent,
    timeFormatter: DateTimeFormatter
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Box(
            modifier = GlanceModifier
                .size(4.dp, 24.dp)
                .background(GlanceTheme.colors.primary)
        ) {}

        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = event.title,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            if (!event.isAllDay) {
                val startTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(event.startTimeMillis),
                    ZoneId.systemDefault()
                )
                Text(
                    text = startTime.format(timeFormatter),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}