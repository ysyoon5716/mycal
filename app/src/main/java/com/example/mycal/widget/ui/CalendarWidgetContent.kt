package com.example.mycal.widget.ui

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.example.mycal.MainActivity
import com.example.mycal.widget.state.CalendarWidgetState

@Composable
fun CalendarWidgetContent() {
    val size = LocalSize.current
    val state = currentState<CalendarWidgetState>()

    Log.d("CalendarWidgetContent", "Rendering widget with size: $size")

    val clickAction = actionStartActivity(
        Intent(androidx.glance.LocalContext.current, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick = clickAction)
    ) {
        when {
            size.width <= 150.dp && size.height <= 150.dp -> {
                // Small widget (2x2)
                SmallWidgetContent(state)
            }
            size.width > 150.dp && size.height <= 150.dp -> {
                // Medium widget (4x2)
                MediumWidgetContent(state)
            }
            size.width >= 310.dp && size.height >= 310.dp -> {
                // Extra Large widget (5x5)
                ExtraLargeWidgetContent(state)
            }
            else -> {
                // Large widget (4x4)
                LargeWidgetContent(state)
            }
        }
    }
}