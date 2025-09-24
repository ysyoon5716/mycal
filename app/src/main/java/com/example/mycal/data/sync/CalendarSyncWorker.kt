package com.example.mycal.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.mycal.data.local.dao.CalendarSourceDao
import com.example.mycal.data.local.dao.EventDao
import com.example.mycal.data.remote.datasource.IcsRemoteDataSource
import com.example.mycal.data.remote.datasource.IcsResult
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

            if (sourceId != null) {
                syncSingleSource(sourceId)
            } else {
                syncAllSources()
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun syncSingleSource(sourceId: String) {
        val source = calendarSourceDao.getSourceById(sourceId) ?: return

        if (!source.syncEnabled) return

        val result = icsRemoteDataSource.fetchAndParseIcs(
            url = source.url,
            sourceId = source.id,
            sourceColor = source.color,
            etag = null
        )

        when (result) {
            is IcsResult.Success -> {
                eventDao.deleteEventsBySource(sourceId)
                eventDao.insertEvents(result.events)
                calendarSourceDao.updateSyncTime(sourceId, System.currentTimeMillis())
            }
            is IcsResult.NotModified -> {
                calendarSourceDao.updateSyncTime(sourceId, System.currentTimeMillis())
            }
            is IcsResult.Error -> {
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

    companion object {
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