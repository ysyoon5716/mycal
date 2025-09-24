package com.example.mycal.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycal.domain.model.CalendarDate
import com.example.mycal.domain.model.CalendarEvent
import com.example.mycal.domain.model.CalendarViewMode
import com.example.mycal.domain.usecase.GetMonthEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMonthEventsUseCase: GetMonthEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCurrentMonth()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onViewModeChanged(mode: CalendarViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = yearMonth) }
        loadMonthData(yearMonth)
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
        loadCurrentMonth()
    }

    private fun loadCurrentMonth() {
        loadMonthData(_uiState.value.currentMonth)
    }

    private fun loadMonthData(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                getMonthEventsUseCase(yearMonth.year, yearMonth.monthValue)
                    .collect { events ->
                        val calendarDates = generateCalendarDates(yearMonth, events)
                        _uiState.update {
                            it.copy(
                                calendarDates = calendarDates,
                                events = events,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
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
            event.isOnDate(date.atStartOfDay())
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