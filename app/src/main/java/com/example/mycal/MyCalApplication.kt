package com.example.mycal

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.mycal.domain.usecase.DefaultDataInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.threeten.bp.zone.TzdbZoneRulesProvider
import org.threeten.bp.zone.ZoneRulesProvider
import javax.inject.Inject

@HiltAndroidApp
class MyCalApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var defaultDataInitializer: DefaultDataInitializer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize ThreeTenBP for date/time handling
        try {
            ZoneRulesProvider.registerProvider(TzdbZoneRulesProvider())
        } catch (e: Exception) {
            // Provider may already be registered
        }

        // Initialize default data (Korean holidays)
        applicationScope.launch {
            defaultDataInitializer.initialize()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}