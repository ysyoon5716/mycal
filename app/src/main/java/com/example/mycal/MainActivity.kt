package com.example.mycal

import android.content.Intent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mycal.presentation.screens.calendar.CalendarScreen
import com.example.mycal.presentation.screens.subscription.SubscriptionListScreen
import com.example.mycal.presentation.theme.MyCalTheme
import com.example.mycal.widget.CalendarAppWidget
import com.example.mycal.widget.CalendarWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract date from intent if available
        val selectedYear = intent.getIntExtra("selected_year", -1)
        val selectedMonth = intent.getIntExtra("selected_month", -1)
        val selectedDay = intent.getIntExtra("selected_day", -1)

        val initialDate = if (selectedYear != -1 && selectedMonth != -1 && selectedDay != -1) {
            org.threeten.bp.LocalDate.of(selectedYear, selectedMonth, selectedDay)
        } else {
            null
        }

        setContent {
            MyCalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalendarApp(initialDate = initialDate)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update widget when app becomes visible
        updateWidget()
    }

    private fun updateWidget() {
        val intent = Intent(this, CalendarWidgetReceiver::class.java).apply {
            action = CalendarAppWidget.ACTION_UPDATE_WIDGET
        }
        sendBroadcast(intent)
    }
}

@Composable
fun CalendarApp(initialDate: org.threeten.bp.LocalDate? = null) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "calendar"
    ) {
        composable("calendar") {
            CalendarScreen(
                modifier = Modifier.fillMaxSize(),
                initialDate = initialDate,
                onNavigateToSubscriptions = {
                    navController.navigate("subscriptions")
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