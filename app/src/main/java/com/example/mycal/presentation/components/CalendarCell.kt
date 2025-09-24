package com.example.mycal.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycal.domain.model.CalendarDate
import com.example.mycal.presentation.theme.*

@Composable
fun CalendarCell(
    date: CalendarDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        date.isSelected -> CalendarSelectedBackground
        date.isToday -> CalendarTodayBackground
        else -> Color.Transparent
    }

    val textColor = when {
        date.isSelected -> Color.White
        !date.isCurrentMonth -> CalendarInactiveDate
        date.isWeekend -> CalendarWeekendText
        else -> MaterialTheme.colorScheme.onSurface
    }

    val fontWeight = when {
        date.isToday -> FontWeight.Bold
        else -> FontWeight.Normal
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = date.isCurrentMonth) { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Event indicators
            if (date.hasEvents) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val eventsToShow = minOf(date.events.size, 3)
                    repeat(eventsToShow) {
                        EventDot(
                            color = CalendarEventIndicator,
                            modifier = Modifier.size(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
    )
}