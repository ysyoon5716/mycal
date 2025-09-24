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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalendarApp()
                }
            }
        }
    }
}

@Composable
fun CalendarApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "calendar"
    ) {
        composable("calendar") {
            CalendarScreen(
                modifier = Modifier.fillMaxSize(),
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