package com.example.mycal.data.sync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodicSync() {
        val request = CalendarSyncWorker.createPeriodicWorkRequest()

        workManager.enqueueUniquePeriodicWork(
            CalendarSyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun syncNow(sourceId: String? = null) {
        val request = CalendarSyncWorker.createOneTimeWorkRequest(sourceId)

        if (sourceId != null) {
            workManager.enqueueUniqueWork(
                "${CalendarSyncWorker.WORK_NAME_ONE_TIME}_$sourceId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        } else {
            workManager.enqueue(request)
        }
    }

    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(CalendarSyncWorker.WORK_NAME_PERIODIC)
    }

    fun cancelSync(sourceId: String) {
        workManager.cancelUniqueWork("${CalendarSyncWorker.WORK_NAME_ONE_TIME}_$sourceId")
    }

    fun getSyncWorkInfo(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(CalendarSyncWorker.WORK_NAME_PERIODIC)
    }
}