package com.example.mycal.widget

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class CalendarWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "CalendarWidgetWorker"
        private const val WORK_NAME = "calendar_widget_update"

        fun enqueuePeriodicWork(context: Context) {
            Log.d(TAG, "Enqueueing periodic widget update work")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<CalendarWidgetWorker>(
                30, TimeUnit.MINUTES,  // Repeat every 30 minutes
                15, TimeUnit.MINUTES   // Flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodicWorkRequest
                )
        }

        fun cancelWork(context: Context) {
            Log.d(TAG, "Cancelling widget update work")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun enqueueOneTimeWork(context: Context) {
            Log.d(TAG, "Enqueueing one-time widget update")

            val workRequest = OneTimeWorkRequestBuilder<CalendarWidgetWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting widget update work")

            // Update all widgets
            CalendarWidget().updateAllWidgets(context)

            Log.d(TAG, "Widget update completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widgets", e)
            Result.retry()
        }
    }
}