package com.example.mycal.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import com.example.mycal.widget.state.CalendarWidgetState
import com.example.mycal.widget.state.CalendarWidgetStateDefinition
import com.example.mycal.widget.ui.CalendarWidgetContent

class CalendarWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "CalendarWidget"
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp),  // Small (2x2)
            DpSize(250.dp, 110.dp),  // Medium (4x2)
            DpSize(250.dp, 250.dp)   // Large (4x4)
        )
    )

    override val stateDefinition = CalendarWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "provideGlance called for widget id: $id")

        // Fetch fresh data for the widget
        val dataProvider = CalendarWidgetDataProvider(context)
        val state = dataProvider.getWidgetState()

        // Update state in DataStore
        stateDefinition.getDataStore(context, "widget_$id").updateData { state }

        provideContent {
            GlanceTheme {
                CalendarWidgetContent()
            }
        }
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        Log.d(TAG, "Widget deleted: $glanceId")
    }

    suspend fun updateAllWidgets(context: Context) {
        Log.d(TAG, "Updating all widgets")
        updateAll(context)
    }
}