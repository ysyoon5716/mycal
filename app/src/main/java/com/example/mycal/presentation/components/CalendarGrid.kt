package com.example.mycal.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mycal.domain.model.CalendarDate
import org.threeten.bp.LocalDate

@Composable
fun CalendarGrid(
    dates: List<CalendarDate>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = dates,
            key = { it.date.toEpochDay() }
        ) { calendarDate ->
            CalendarCell(
                date = calendarDate,
                onClick = { onDateClick(calendarDate.date) },
                modifier = Modifier.aspectRatio(1f)
            )
        }
    }
}