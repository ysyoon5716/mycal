package com.example.mycal.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.mycal.MainActivity
import com.example.mycal.widget.state.CalendarWidgetState

@Composable
fun CalendarMonthWidget(
    state: CalendarWidgetState,
    onPreviousMonth: Action,
    onNextMonth: Action
) {
    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF121212)))
                .cornerRadius(16.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                LoadingView()
            } else {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Header with month navigation
                    WidgetHeader(
                        monthName = state.monthName,
                        year = state.year,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        onAddEvent = actionStartActivity(Intent(LocalContext.current, MainActivity::class.java)),
                        modifier = GlanceModifier.fillMaxWidth()
                    )

                    // Calendar grid
                    WidgetCalendarGrid(
                        dates = state.calendarDates,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "캘린더 로딩중...",
            style = TextStyle(
                color = ColorProvider(Color(0xFF9E9E9E)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        )
    }
}