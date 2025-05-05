package com.matanboas.maslul

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.matanboas.maslul.ui.theme.MaslulTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaslulTheme {
                    AppNavHost()
                }
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onNavigateToRoutes = { navController.navigate("routes_list") },
                onNavigateToMap = { navController.navigate("map_view") }
            )
        }

        composable("routes_list") {
            RoutesListScreen(
                onRouteClick = { routeId -> navController.navigate("route_detail/$routeId") }
            )
        }

        composable("map_view") {
            MapViewScreen(
                onRouteSelected = { routeId -> navController.navigate("route_detail/$routeId") }
            )
        }

        composable(
            route = "route_detail/{routeId}",
            arguments = listOf(navArgument("routeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
            RouteDetailScreen(routeId)
        }
    }
}
