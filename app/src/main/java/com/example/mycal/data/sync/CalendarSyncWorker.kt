package com.example.mycal.data.sync

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.mycal.data.local.dao.CalendarSourceDao
import com.example.mycal.data.local.dao.EventDao
import com.example.mycal.data.remote.datasource.IcsRemoteDataSource
import com.example.mycal.data.remote.datasource.IcsResult
import com.example.mycal.widget.CalendarAppWidget
import com.example.mycal.widget.CalendarWidgetReceiver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class CalendarSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val calendarSourceDao: CalendarSourceDao,
    private val eventDao: EventDao,
    private val icsRemoteDataSource: IcsRemoteDataSource
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val sourceId = inputData.getString(KEY_SOURCE_ID)
            Log.d(TAG, "Starting sync work for sourceId: $sourceId")

            if (sourceId != null) {
                syncSingleSource(sourceId)
            } else {
                syncAllSources()
            }

            // Trigger widget update after successful sync
            updateWidgets()

            Log.d(TAG, "Sync work completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync work failed", e)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun syncSingleSource(sourceId: String) {
        Log.d(TAG, "Syncing single source: $sourceId")
        val source = calendarSourceDao.getSourceById(sourceId)
        if (source == null) {
            Log.w(TAG, "Source not found: $sourceId")
            return
        }

        if (!source.syncEnabled) {
            Log.d(TAG, "Sync disabled for source: $sourceId")
            return
        }

        Log.d(TAG, "Fetching ICS from URL: ${source.url}")
        val result = icsRemoteDataSource.fetchAndParseIcs(
            url = source.url,
            sourceId = source.id,
            sourceColor = source.color,
            etag = null
        )

        when (result) {
            is IcsResult.Success -> {
                Log.d(TAG, "ICS fetch successful, got ${result.events.size} events")
                eventDao.deleteEventsBySource(sourceId)
                eventDao.insertEvents(result.events)
                calendarSourceDao.updateSyncTime(sourceId, System.currentTimeMillis())
                Log.d(TAG, "Events saved to database for source: $sourceId")
            }
            is IcsResult.NotModified -> {
                Log.d(TAG, "ICS not modified for source: $sourceId")
                calendarSourceDao.updateSyncTime(sourceId, System.currentTimeMillis())
            }
            is IcsResult.Error -> {
                Log.e(TAG, "Failed to sync source $sourceId: ${result.message}")
                throw Exception(result.message)
            }
        }
    }

    private suspend fun syncAllSources() {
        val activeSources = calendarSourceDao.getActiveSources()
        activeSources.collect { sources ->
            sources.forEach { source ->
                try {
                    syncSingleSource(source.id)
                } catch (e: Exception) {
                    // Continue syncing other sources even if one fails
                }
            }
        }
    }

    private fun updateWidgets() {
        try {
            val intent = Intent(applicationContext, CalendarWidgetReceiver::class.java).apply {
                action = CalendarAppWidget.ACTION_UPDATE_WIDGET
            }
            applicationContext.sendBroadcast(intent)
            Log.d(TAG, "Widget update broadcast sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send widget update broadcast", e)
        }
    }

    companion object {
        private const val TAG = "CalendarSyncWorker"
        const val KEY_SOURCE_ID = "source_id"
        const val WORK_NAME_PERIODIC = "calendar_sync_periodic"
        const val WORK_NAME_ONE_TIME = "calendar_sync_one_time"
        private const val MAX_RETRY_ATTEMPTS = 3

        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            return PeriodicWorkRequestBuilder<CalendarSyncWorker>(
                6, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.MINUTES
                )
                .build()
        }

        fun createOneTimeWorkRequest(sourceId: String? = null): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = workDataOf(
                KEY_SOURCE_ID to sourceId
            )

            return OneTimeWorkRequestBuilder<CalendarSyncWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()
        }
    }
}