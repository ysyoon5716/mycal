package com.example.mycal

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.threeten.bp.zone.TzdbZoneRulesProvider
import org.threeten.bp.zone.ZoneRulesProvider

@HiltAndroidApp
class MyCalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize ThreeTenBP for date/time handling
        try {
            ZoneRulesProvider.registerProvider(TzdbZoneRulesProvider())
        } catch (e: Exception) {
            // Provider may already be registered
        }
    }
}