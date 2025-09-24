package com.example.mycal.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        private const val TAG = "CalendarWidgetReceiver"
        const val ACTION_UPDATE_WIDGET = "com.example.mycal.widget.UPDATE"
    }

    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Log.d(TAG, "onReceive: ${intent.action}")

        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                Log.d(TAG, "Received update widget action")
                updateAllWidgets(context)
            }
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                Log.d(TAG, "System time/date/timezone changed, updating widgets")
                updateAllWidgets(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate called for widget IDs: ${appWidgetIds.contentToString()}")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "Widget(s) deleted: ${appWidgetIds.contentToString()}")
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "First widget added")
        // Start periodic update worker when first widget is added
        CalendarWidgetWorker.enqueuePeriodicWork(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Last widget removed")
        // Cancel periodic update worker when last widget is removed
        CalendarWidgetWorker.cancelWork(context)
    }

    private fun updateAllWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                (glanceAppWidget as CalendarWidget).updateAllWidgets(context)
                Log.d(TAG, "All widgets updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets", e)
            }
        }
    }
}