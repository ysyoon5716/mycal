package com.example.mycal.domain.usecase

import com.example.mycal.domain.model.CalendarEvent
import com.example.mycal.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMonthEventsUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    operator fun invoke(year: Int, month: Int): Flow<List<CalendarEvent>> {
        return repository.getEventsForMonth(year, month)
    }
}