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
import androidx.glance.unit.ColorProvider
import com.example.mycal.widget.state.CalendarWidgetState
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle as DateTextStyle
import java.util.Locale

@Composable
fun SmallWidgetContent(state: CalendarWidgetState) {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.getDefault())
    val monthYear = today.format(DateTimeFormatter.ofPattern("MMM yyyy"))
    val eventCount = state.todayEvents.size

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day of month (large)
        Text(
            text = today.dayOfMonth.toString(),
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        )

        // Day of week
        Text(
            text = dayOfWeek.uppercase(),
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Month and Year
        Text(
            text = monthYear,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp
            )
        )

        if (eventCount > 0) {
            Spacer(modifier = GlanceModifier.height(8.dp))

            // Event indicator
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(GlanceTheme.colors.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$eventCount ${if (eventCount == 1) "event" else "events"}",
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}