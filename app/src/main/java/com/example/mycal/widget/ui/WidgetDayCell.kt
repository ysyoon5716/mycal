package com.example.mycal.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.mycal.MainActivity
import com.example.mycal.widget.model.WidgetCalendarDate

// Maximum number of events to display per day cell
// Increase this value to test UI with more events
private const val MAX_EVENTS_PER_DAY = 4

@Composable
fun WidgetDayCell(
    date: WidgetCalendarDate,
    modifier: GlanceModifier = GlanceModifier
) {
    val dateTextColor = when {
        date.isToday -> ColorProvider(Color.White)
        !date.isCurrentMonth -> ColorProvider(Color(0xFF757575))
        date.isSunday -> ColorProvider(Color(0xFFEF5350))
        date.isSaturday -> ColorProvider(Color(0xFF42A5F5))
        else -> ColorProvider(Color(0xFFE0E0E0))
    }

    // Create border effect for today's cell using nested Box
    if (date.isToday) {
        Box(
            modifier = modifier
                .cornerRadius(4.dp)
                .background(ColorProvider(Color.White))  // White border
                .padding(1.dp),  // Border thickness
            contentAlignment = Alignment.TopStart
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(3.dp)
                    .background(ColorProvider(Color(0xFF121212)))  // Dark background inside border
                    .clickable(actionStartActivity(Intent(LocalContext.current, MainActivity::class.java).apply {
                    putExtra("selected_year", date.year)
                    putExtra("selected_month", date.month)
                    putExtra("selected_day", date.dayOfMonth)
                }))
                    .padding(2.dp),
                contentAlignment = Alignment.TopStart
            ) {
                DayCellContent(date, dateTextColor)
            }
        }
    } else {
        Box(
            modifier = modifier
                .cornerRadius(4.dp)
                .background(ColorProvider(Color.Transparent))
                .clickable(actionStartActivity(Intent(LocalContext.current, MainActivity::class.java).apply {
                    putExtra("selected_year", date.year)
                    putExtra("selected_month", date.month)
                    putExtra("selected_day", date.dayOfMonth)
                }))
                .padding(2.dp),
            contentAlignment = Alignment.TopStart
        ) {
            DayCellContent(date, dateTextColor)
        }
    }
}

@Composable
private fun DayCellContent(
    date: WidgetCalendarDate,
    dateTextColor: ColorProvider
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Date number
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = TextStyle(
                    color = dateTextColor,
                    fontSize = 12.sp,
                    fontWeight = if (date.isToday) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            )
        }

        // Events list
        if (date.hasEvents) {
            Spacer(GlanceModifier.height(1.dp))
            val displayEvents = date.getDisplayEvents(MAX_EVENTS_PER_DAY)

            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                displayEvents.forEach { event ->
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Event indicator dot
                        Box(
                            modifier = GlanceModifier
                                .size(3.dp)
                                .cornerRadius(2.dp)
                                .background(ColorProvider(Color(event.color)))
                        ) {}

                        Spacer(GlanceModifier.width(2.dp))

                        // Event title
                        Text(
                            text = event.getTruncatedTitle(8),
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFBDBDBD)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            maxLines = 1
                        )
                    }
                }

                // More events indicator
                if (date.hasMoreEvents(MAX_EVENTS_PER_DAY)) {
                    Text(
                        text = "+${date.getMoreEventsCount(MAX_EVENTS_PER_DAY)}",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF9E9E9E)),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = GlanceModifier.padding(start = 5.dp)
                    )
                }
            }
        }
    }
}