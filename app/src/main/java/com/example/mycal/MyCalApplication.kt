package com.example.mycal

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.threeten.bp.zone.TzdbZoneRulesProvider
import org.threeten.bp.zone.ZoneRulesProvider
import javax.inject.Inject

@HiltAndroidApp
class MyCalApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize ThreeTenBP for date/time handling
        try {
            ZoneRulesProvider.registerProvider(TzdbZoneRulesProvider())
        } catch (e: Exception) {
            // Provider may already be registered
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}