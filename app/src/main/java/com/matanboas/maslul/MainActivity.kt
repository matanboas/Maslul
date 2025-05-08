package com.matanboas.maslul

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matanboas.maslul.ui.screens.HomeScreen
import com.matanboas.maslul.ui.theme.MaslulTheme
import com.matanboas.maslul.network.TrailService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val trailService = TrailService()

        setContent {
            MaslulTheme {
                AppNavigation(trailService = trailService, context = applicationContext)
            }
        }
    }
}

@Composable
fun AppNavigation(trailService: TrailService, context: Context) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(trailService = trailService, context = context)
        }
    }
}