package com.example.mycal.domain.usecase

import com.example.mycal.domain.model.CalendarSource
import com.example.mycal.domain.repository.CalendarSourceRepository
import java.util.UUID
import javax.inject.Inject

class AddCalendarSourceUseCase @Inject constructor(
    private val repository: CalendarSourceRepository
) {
    suspend operator fun invoke(
        url: String,
        name: String,
        color: Int
    ): Result<Unit> {
        return try {
            val source = CalendarSource(
                id = UUID.randomUUID().toString(),
                url = url,
                name = name,
                color = color,
                syncEnabled = true,
                lastSyncTime = null
            )
            repository.addSource(source)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}