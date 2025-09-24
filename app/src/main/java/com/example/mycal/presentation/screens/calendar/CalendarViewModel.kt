package com.example.mycal.presentation.screens.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycal.domain.model.CalendarDate
import com.example.mycal.domain.model.CalendarEvent
import com.example.mycal.domain.model.CalendarViewMode
import com.example.mycal.domain.usecase.GetMonthEventsUseCase
import com.example.mycal.data.local.dao.EventDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.Job
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMonthEventsUseCase: GetMonthEventsUseCase,
    private val eventDao: EventDao
) : ViewModel() {

    companion object {
        private const val TAG = "CalendarViewModel"
    }

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val monthDataCache = mutableMapOf<YearMonth, List<CalendarDate>>()
    private val monthLoadingJobs = mutableMapOf<YearMonth, Job>()

    init {
        loadCurrentMonthWithAdjacent()
        checkDatabaseEvents()
    }

    private fun checkDatabaseEvents() {
        viewModelScope.launch {
            val totalEvents = eventDao.getTotalEventCount()
            Log.d(TAG, "Total events in database at startup: $totalEvents")

            if (totalEvents > 0) {
                val allEvents = eventDao.getAllEvents()
                allEvents.take(5).forEach { event ->
                    val startDate = org.threeten.bp.Instant.ofEpochMilli(event.startTime)
                        .atZone(org.threeten.bp.ZoneId.systemDefault())
                        .toLocalDateTime()
                    Log.d(TAG, "DB Event: ${event.title}, Start: $startDate, Source: ${event.sourceId}")
                }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        Log.d(TAG, "Date selected: $date")

        // Update selected date
        _uiState.update { currentState ->
            // Update monthDataCache with new selected state
            val updatedMonthDataMap = monthDataCache.mapValues { (yearMonth, dates) ->
                dates.map { calendarDate ->
                    calendarDate.copy(isSelected = calendarDate.date == date)
                }
            }

            // Update the cache
            monthDataCache.clear()
            monthDataCache.putAll(updatedMonthDataMap)

            // Return updated state
            currentState.copy(
                selectedDate = date,
                calendarDates = updatedMonthDataMap[currentState.currentMonth] ?: currentState.calendarDates,
                monthDataMap = updatedMonthDataMap
            )
        }
    }

    fun getSelectedDateEvents(): List<CalendarEvent> {
        val selectedDate = _uiState.value.selectedDate
        val allEvents = _uiState.value.events

        val selectedEvents = allEvents.filter { event ->
            event.isOnDate(selectedDate.atStartOfDay())
        }.sortedBy { it.startTime }

        Log.d(TAG, "Selected date: $selectedDate, Events: ${selectedEvents.size}")
        selectedEvents.forEach { event ->
            Log.d(TAG, "Event on selected date: ${event.title}, Time: ${event.startTime}")
        }

        return selectedEvents
    }

    fun onViewModeChanged(mode: CalendarViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = yearMonth) }
        loadMonthDataWithAdjacent(yearMonth)
    }

    fun navigateToPreviousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        onMonthChanged(newMonth)
    }

    fun navigateToNextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        onMonthChanged(newMonth)
    }

    fun navigateToToday() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                currentMonth = YearMonth.from(today),
                selectedDate = today
            )
        }
        loadCurrentMonthWithAdjacent()
    }

    private fun loadCurrentMonth() {
        loadMonthData(_uiState.value.currentMonth)
    }

    private fun loadCurrentMonthWithAdjacent() {
        val currentMonth = _uiState.value.currentMonth
        loadMonthDataWithAdjacent(currentMonth)
    }

    private fun loadMonthDataWithAdjacent(yearMonth: YearMonth) {
        // Load current month and adjacent months
        val monthsToLoad = listOf(
            yearMonth.minusMonths(1),
            yearMonth,
            yearMonth.plusMonths(1)
        )

        // Clear cache for current month to force reload
        monthDataCache.remove(yearMonth)

        monthsToLoad.forEach { month ->
            loadMonthData(month)
        }
    }

    private fun loadMonthData(yearMonth: YearMonth) {
        // Cancel existing job for this month if it exists
        monthLoadingJobs[yearMonth]?.cancel()

        monthLoadingJobs[yearMonth] = viewModelScope.launch {
            // Only show loading for the current month
            if (yearMonth == _uiState.value.currentMonth) {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                // Add timeout to prevent infinite waiting
                // Also check database directly
                viewModelScope.launch {
                    val dbEventsCount = eventDao.getTotalEventCount()
                    Log.d(TAG, "Database has $dbEventsCount total events")
                }

                // Use take(1) to get only the first emission
                getMonthEventsUseCase(yearMonth.year, yearMonth.monthValue)
                    .take(1)
                    .collect { events ->
                            Log.d(TAG, "UseCase returned ${events.size} events for ${yearMonth.year}-${yearMonth.monthValue}")
                            events.take(3).forEach { event ->
                                Log.d(TAG, "UseCase Event: ${event.title}, Date: ${event.startTime}")
                            }
                            val calendarDates = generateCalendarDates(yearMonth, events)

                            // Cache the data
                            monthDataCache[yearMonth] = calendarDates

                            // Update UI state
                            if (yearMonth == _uiState.value.currentMonth) {
                                _uiState.update {
                                    it.copy(
                                        calendarDates = calendarDates,
                                        events = events,
                                        monthDataMap = monthDataCache.toMap(),
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            } else {
                                // Just update the cache in UI state
                                _uiState.update {
                                    it.copy(
                                        monthDataMap = monthDataCache.toMap()
                                    )
                                }
                        }
                    }
            } catch (e: Exception) {
                // On error, show empty calendar
                val calendarDates = generateCalendarDates(yearMonth, emptyList())
                monthDataCache[yearMonth] = calendarDates

                if (yearMonth == _uiState.value.currentMonth) {
                    _uiState.update {
                        it.copy(
                            calendarDates = calendarDates,
                            events = emptyList(),
                            monthDataMap = monthDataCache.toMap(),
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
            }
        }
    }

    private fun updateUiStateFromCache() {
        val currentMonth = _uiState.value.currentMonth
        val currentMonthData = monthDataCache[currentMonth]

        if (currentMonthData != null) {
            _uiState.update {
                it.copy(
                    calendarDates = currentMonthData,
                    monthDataMap = monthDataCache.toMap(),
                    isLoading = false
                )
            }
        }
    }

    private fun generateCalendarDates(
        yearMonth: YearMonth,
        events: List<CalendarEvent>
    ): List<CalendarDate> {
        val dates = mutableListOf<CalendarDate>()
        val firstOfMonth = yearMonth.atDay(1)
        val lastOfMonth = yearMonth.atEndOfMonth()

        // Add dates from previous month to fill the first week
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Sunday = 0
        if (firstDayOfWeek > 0) {
            for (i in firstDayOfWeek downTo 1) {
                val date = firstOfMonth.minusDays(i.toLong())
                dates.add(createCalendarDate(date, events, false))
            }
        }

        // Add dates of current month
        for (day in 1..lastOfMonth.dayOfMonth) {
            val date = yearMonth.atDay(day)
            dates.add(createCalendarDate(date, events, true))
        }

        // Add dates from next month to fill the last week
        val remainingDays = 42 - dates.size // 6 weeks * 7 days
        for (i in 1..remainingDays) {
            val date = lastOfMonth.plusDays(i.toLong())
            dates.add(createCalendarDate(date, events, false))
        }

        return dates
    }

    private fun createCalendarDate(
        date: LocalDate,
        events: List<CalendarEvent>,
        isCurrentMonth: Boolean
    ): CalendarDate {
        val dateEvents = events.filter { event ->
            val matches = event.isOnDate(date.atStartOfDay())
            if (matches) {
                Log.d(TAG, "Event ${event.title} matches date $date")
            }
            matches
        }

        return CalendarDate(
            date = date,
            isToday = date == LocalDate.now(),
            isSelected = date == _uiState.value.selectedDate,
            isCurrentMonth = isCurrentMonth,
            events = dateEvents
        )
    }
}