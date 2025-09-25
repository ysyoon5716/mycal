package com.example.mycal.presentation.screens.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycal.domain.model.CalendarDate
import com.example.mycal.domain.model.CalendarEvent
import com.example.mycal.domain.usecase.GetMonthEventsUseCase
import com.example.mycal.data.local.dao.EventDao
import com.example.mycal.domain.event.SyncEventManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.FlowPreview
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMonthEventsUseCase: GetMonthEventsUseCase,
    private val eventDao: EventDao,
    private val syncEventManager: SyncEventManager
) : ViewModel() {

    companion object {
        private const val TAG = "CalendarViewModel"
    }

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _selectedDateEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val selectedDateEvents: StateFlow<List<CalendarEvent>> = _selectedDateEvents.asStateFlow()

    private val monthDataCache = mutableMapOf<YearMonth, List<CalendarDate>>()
    private val monthLoadingJobs = mutableMapOf<YearMonth, Job>()
    private var currentMonthSubscription: Job? = null

    init {
        loadCurrentMonthWithAdjacent()
        checkDatabaseEvents()
        observeSyncEvents()
        // Initialize selected date events
        updateSelectedDateEvents()
        // Start observing after initial load
        viewModelScope.launch {
            delay(1500)
            startObservingCurrentMonth()
        }
    }

    private fun observeCurrentMonthEvents() {
        // This function sets up real-time observation ONLY when not manually refreshing
    }

    private fun startObservingCurrentMonth() {
        // Cancel any existing subscription first
        currentMonthSubscription?.cancel()

        val currentMonth = _uiState.value.currentMonth

        // Subscribe to current month events with debounce
        currentMonthSubscription = viewModelScope.launch {
            // Wait a bit after initial load to avoid conflicts
            delay(1000)

            getMonthEventsUseCase(currentMonth.year, currentMonth.monthValue)
                .debounce(500) // Longer debounce for stability
                .distinctUntilChanged { old, new ->
                    // Compare by size and IDs
                    old.size == new.size &&
                    old.map { it.id }.toSet() == new.map { it.id }.toSet()
                }
                .collect { events ->
                    Log.d(TAG, "Real-time update: ${events.size} events for $currentMonth")
                    val calendarDates = generateCalendarDates(currentMonth, events)

                    // Update cache
                    monthDataCache[currentMonth] = calendarDates

                    // Update UI state
                    _uiState.update {
                        if (it.currentMonth == currentMonth) { // Only update if still on same month
                            it.copy(
                                calendarDates = calendarDates,
                                events = events,
                                monthDataMap = monthDataCache.toMap()
                            )
                        } else {
                            it
                        }
                    }

                    // Update selected date events
                    updateSelectedDateEvents()
                }
        }
    }

    private fun observeSyncEvents() {
        viewModelScope.launch {
            syncEventManager.syncCompletedEvent.collect {
                Log.d(TAG, "Sync completed event received, refreshing calendar")
                refreshCurrentMonth()
            }
        }
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

        // Update selected date events
        updateSelectedDateEvents()
    }

    private fun updateSelectedDateEvents() {
        val selectedDate = _uiState.value.selectedDate
        val allEvents = _uiState.value.events

        val events = allEvents.filter { event ->
            event.isOnDate(selectedDate.atStartOfDay())
        }.sortedWith(compareBy({ it.startTime }, { it.id })) // Secondary sort by ID for stability

        // Only update if the events have actually changed
        val currentEvents = _selectedDateEvents.value
        if (currentEvents.size != events.size ||
            currentEvents.map { it.id } != events.map { it.id }) {
            Log.d(TAG, "Updating selected date events: ${events.size} events for $selectedDate")
            _selectedDateEvents.value = events
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

    fun onMonthChanged(yearMonth: YearMonth) {
        // Cancel current subscription before changing month
        currentMonthSubscription?.cancel()

        _uiState.update { it.copy(currentMonth = yearMonth) }
        loadMonthDataWithAdjacent(yearMonth)

        // Start observing the new month after loading
        viewModelScope.launch {
            delay(1500) // Wait for initial load
            startObservingCurrentMonth()
        }
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

    fun refreshCurrentMonth() {
        Log.d(TAG, "Refreshing current month data")

        // Cancel real-time subscription during refresh
        currentMonthSubscription?.cancel()

        // Clear cache for current month and adjacent months to force reload
        val currentMonth = _uiState.value.currentMonth
        monthDataCache.remove(currentMonth.minusMonths(1))
        monthDataCache.remove(currentMonth)
        monthDataCache.remove(currentMonth.plusMonths(1))

        // Cancel existing loading jobs
        monthLoadingJobs[currentMonth.minusMonths(1)]?.cancel()
        monthLoadingJobs[currentMonth]?.cancel()
        monthLoadingJobs[currentMonth.plusMonths(1)]?.cancel()

        // Reload the data
        loadCurrentMonthWithAdjacent()

        // Restart observation after reload is complete
        viewModelScope.launch {
            delay(1500) // Wait for reload to complete
            startObservingCurrentMonth()
        }
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

                // Get the first emission from the Flow
                val events = getMonthEventsUseCase(yearMonth.year, yearMonth.monthValue)
                    .first()

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
                    // Update selected date events when current month events change
                    updateSelectedDateEvents()
                } else {
                    // Just update the cache in UI state
                    _uiState.update {
                        it.copy(
                            monthDataMap = monthDataCache.toMap()
                        )
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