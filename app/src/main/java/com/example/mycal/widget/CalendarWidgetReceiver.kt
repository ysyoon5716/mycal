package com.example.mycal.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CalendarAppWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            CalendarAppWidget.ACTION_UPDATE_WIDGET,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED -> {
                enqueueWidgetUpdate(context)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        enqueueWidgetUpdate(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        enqueueWidgetUpdate(context)
    }

    private fun enqueueWidgetUpdate(context: Context) {
        val updateRequest = OneTimeWorkRequestBuilder<CalendarWidgetWorker>()
            .build()

        WorkManager.getInstance(context).enqueue(updateRequest)
    }
}