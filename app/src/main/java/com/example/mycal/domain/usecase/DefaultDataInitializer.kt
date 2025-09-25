package com.example.mycal.domain.usecase

import android.util.Log
import com.example.mycal.data.local.dao.CalendarSourceDao
import com.example.mycal.data.local.entity.CalendarSourceEntity
import com.example.mycal.domain.model.DefaultSubscriptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDataInitializer @Inject constructor(
    private val calendarSourceDao: CalendarSourceDao,
    private val addCalendarSourceUseCase: AddCalendarSourceUseCase
) {
    companion object {
        private const val TAG = "DefaultDataInitializer"
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            // Check if any calendar sources exist
            val existingSources = calendarSourceDao.getAllSources().first()

            if (existingSources.isEmpty()) {
                Log.d(TAG, "No existing calendar sources found. Adding default subscriptions...")

                // Add each default subscription
                DefaultSubscriptions.DEFAULT_SUBSCRIPTIONS.forEach { defaultSource ->
                    try {
                        Log.d(TAG, "Adding default calendar: ${defaultSource.name}")

                        // Use AddCalendarSourceUseCase to properly add and sync the source
                        addCalendarSourceUseCase(
                            url = defaultSource.url,
                            name = defaultSource.name,
                            color = defaultSource.color
                        ).fold(
                            onSuccess = {
                                Log.d(TAG, "Successfully added default calendar: ${defaultSource.name}")
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Failed to add default calendar: ${defaultSource.name}", error)
                                // Fallback: Try to add directly to database without initial sync
                                addDirectlyToDatabase(defaultSource)
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding default calendar: ${defaultSource.name}", e)
                    }
                }

                Log.d(TAG, "Default subscriptions initialization completed")
            } else {
                Log.d(TAG, "Calendar sources already exist (${existingSources.size}). Skipping default initialization.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during default data initialization", e)
        }
    }

    private suspend fun addDirectlyToDatabase(defaultSource: DefaultSubscriptions.DefaultCalendarSource) {
        try {
            val entity = CalendarSourceEntity(
                id = UUID.randomUUID().toString(),
                url = defaultSource.url,
                name = defaultSource.name,
                color = defaultSource.color,
                syncEnabled = true,
                lastSyncTime = 0L
            )
            calendarSourceDao.insertSource(entity)
            Log.d(TAG, "Added calendar directly to database: ${defaultSource.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add calendar directly to database: ${defaultSource.name}", e)
        }
    }
}