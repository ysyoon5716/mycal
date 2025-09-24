package com.example.mycal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mycal.presentation.screens.calendar.CalendarScreen
import com.example.mycal.presentation.screens.subscription.SubscriptionListScreen
import com.example.mycal.presentation.theme.MyCalTheme
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract date from intent if available
        val selectedDateMillis = intent.getLongExtra("selected_date", 0L)
        val selectedDate = if (selectedDateMillis > 0) {
            Instant.ofEpochMilli(selectedDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        } else {
            null
        }

        setContent {
            MyCalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalendarApp(initialSelectedDate = selectedDate)
                }
            }
        }
    }
}

@Composable
fun CalendarApp(initialSelectedDate: LocalDate? = null) {
    val navController = rememberNavController()
    var pendingSelectedDate by remember { mutableStateOf(initialSelectedDate) }

    NavHost(
        navController = navController,
        startDestination = "calendar"
    ) {
        composable("calendar") {
            CalendarScreen(
                modifier = Modifier.fillMaxSize(),
                initialSelectedDate = pendingSelectedDate,
                onNavigateToSubscriptions = {
                    navController.navigate("subscriptions")
                },
                onDateHandled = {
                    pendingSelectedDate = null
                }
            )
        }

        composable("subscriptions") {
            SubscriptionListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyCalTheme {
        CalendarApp()
    }
}