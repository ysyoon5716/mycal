package com.example.mycal.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle

@Composable
fun WidgetHeader(
    monthName: String,
    year: Int,
    onPreviousMonth: Action,
    onNextMonth: Action,
    onAddEvent: Action,
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous month button
        Text(
            text = "◀",
            style = TextStyle(
                color = ColorProvider(Color(0xFFE0E0E0)),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier
                .clickable(onPreviousMonth)
                .padding(4.dp)
        )

        // Small fixed spacer
        Spacer(GlanceModifier.width(8.dp))

        // Month and year - no weight, just natural width
        Text(
            text = getKoreanMonthName(monthName),
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )

        // Small fixed spacer before next button
        Spacer(GlanceModifier.width(8.dp))

        // Next month button
        Text(
            text = "▶",
            style = TextStyle(
                color = ColorProvider(Color(0xFFE0E0E0)),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier
                .clickable(onNextMonth)
                .padding(4.dp)
        )

        // Flexible spacer to push add event button to the right
        Spacer(GlanceModifier.defaultWeight())

        // Add event button
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .clickable(onAddEvent)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "24",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

private fun getKoreanMonthName(monthName: String): String {
    return when (monthName.uppercase()) {
        "JANUARY" -> "1월"
        "FEBRUARY" -> "2월"
        "MARCH" -> "3월"
        "APRIL" -> "4월"
        "MAY" -> "5월"
        "JUNE" -> "6월"
        "JULY" -> "7월"
        "AUGUST" -> "8월"
        "SEPTEMBER" -> "9월"
        "OCTOBER" -> "10월"
        "NOVEMBER" -> "11월"
        "DECEMBER" -> "12월"
        else -> monthName
    }
}