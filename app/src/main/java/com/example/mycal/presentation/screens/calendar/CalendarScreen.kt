package com.example.mycal.presentation.screens.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mycal.presentation.components.CalendarGrid
import com.example.mycal.presentation.components.DailyEventsList
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onNavigateToSubscriptions: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDateEvents by viewModel.selectedDateEvents.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            CalendarTopBar(
                currentMonth = uiState.currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                onPreviousMonth = viewModel::navigateToPreviousMonth,
                onNextMonth = viewModel::navigateToNextMonth,
                onTodayClick = viewModel::navigateToToday,
                onSubscriptionsClick = onNavigateToSubscriptions
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Split screen: Calendar (60%) + Events List (40%)
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Month Calendar - 60% of screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                ) {
                    MonthViewWithSwipe(
                        currentMonth = uiState.currentMonth,
                        monthDataMap = uiState.monthDataMap,
                        onDateSelected = viewModel::onDateSelected,
                        onMonthChanged = viewModel::onMonthChanged,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Daily Events List - 40% of screen
                DailyEventsList(
                    selectedDate = uiState.selectedDate,
                    events = selectedDateEvents,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                )
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error message
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    currentMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onSubscriptionsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous month"
                    )
                }

                Text(
                    text = currentMonth,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next month"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onTodayClick) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Go to today"
                )
            }
            IconButton(onClick = onSubscriptionsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Calendar Subscriptions"
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthViewWithSwipe(
    currentMonth: YearMonth,
    monthDataMap: Map<YearMonth, List<com.example.mycal.domain.model.CalendarDate>>,
    onDateSelected: (org.threeten.bp.LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    // Start at a large number to allow swiping in both directions
    val initialPage = 100000
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 200000 } // Large number to simulate infinite scrolling
    )

    // Track the current page to detect page changes
    LaunchedEffect(pagerState.currentPage) {
        val monthOffset = pagerState.currentPage - initialPage
        val newMonth = YearMonth.now().plusMonths(monthOffset.toLong())
        if (newMonth != currentMonth) {
            onMonthChanged(newMonth)
        }
    }

    Column(modifier = modifier) {
        // Week day headers
        WeekDayHeaders()

        // Calendar grid with swipe
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val monthOffset = page - initialPage
            val displayMonth = YearMonth.now().plusMonths(monthOffset.toLong())

            // Check if we have cached data for this month
            val monthData = monthDataMap[displayMonth]

            if (monthData != null) {
                // Use the cached calendar dates for this month
                CalendarGrid(
                    dates = monthData,
                    onDateClick = onDateSelected,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show loading state for months without cached data
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun WeekDayHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}