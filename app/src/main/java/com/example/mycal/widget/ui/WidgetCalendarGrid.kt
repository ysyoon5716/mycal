package com.example.mycal.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.mycal.widget.model.WidgetCalendarDate

@Composable
fun WidgetCalendarGrid(
    dates: List<WidgetCalendarDate>,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) {
        // Days of week header
        DaysOfWeekHeader()

        // Calendar grid
        val weeks = dates.chunked(7)
        weeks.forEach { week ->
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                week.forEach { date ->
                    WidgetDayCell(
                        date = date,
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(20.dp)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
        val dayColors = listOf(
            ColorProvider(Color(0xFFEF5350)),  // Sunday - Red
            ColorProvider(Color(0xFFE0E0E0)),  // Monday
            ColorProvider(Color(0xFFE0E0E0)),  // Tuesday
            ColorProvider(Color(0xFFE0E0E0)),  // Wednesday
            ColorProvider(Color(0xFFE0E0E0)),  // Thursday
            ColorProvider(Color(0xFFE0E0E0)),  // Friday
            ColorProvider(Color(0xFF42A5F5))   // Saturday - Blue
        )

        daysOfWeek.forEachIndexed { index, day ->
            Box(
                modifier = GlanceModifier.defaultWeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = TextStyle(
                        color = dayColors[index],
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}