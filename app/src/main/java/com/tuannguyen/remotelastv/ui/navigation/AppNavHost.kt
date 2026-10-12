package com.tuannguyen.remotelastv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tuannguyen.remotelastv.ui.home.HomeScreen
import com.tuannguyen.remotelastv.ui.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
    ) {
        composable<Route.Home> {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Route.Settings)
                }
            )
        }
        composable<Route.Settings> {
            SettingsScreen()
        }
    }
}