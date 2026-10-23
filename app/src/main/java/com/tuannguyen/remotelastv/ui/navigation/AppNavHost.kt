package com.tuannguyen.remotelastv.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tuannguyen.remotelastv.ui.device_discovery.DeviceDiscoveryScreen
import com.tuannguyen.remotelastv.ui.faq.FaqScreen
import com.tuannguyen.remotelastv.ui.home.HomeScreen
import com.tuannguyen.remotelastv.ui.language.LanguageScreen
import com.tuannguyen.remotelastv.ui.settings.SettingsScreen
import com.tuannguyen.remotelastv.ui.settings_connected_device.ConnectedDeviceScreen
import com.tuannguyen.remotelastv.ui.settings_connection_display.ConnectionDisplayScreen
import com.tuannguyen.remotelastv.ui.settings_remote_behavior.RemoteBehaviorScreen
import com.tuannguyen.remotelastv.ui.settings_remote_layout.RemoteLayoutScreen
import com.tuannguyen.remotelastv.ui.utils.sendFeedback

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        enterTransition = {
            fadeIn(tween(250))
        },
        exitTransition = {
            fadeOut(tween(200))
        },
        popEnterTransition = {
            fadeIn(tween(250))
        },
        popExitTransition = {
            fadeOut(tween(200))
        },
    ) {
        composable<Route.Home> {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Route.Settings)
                },
                onNavigateToDeviceDiscovery = {
                    navController.navigate(Route.DeviceDiscovery)
                },
            )
        }

        composable<Route.DeviceDiscovery> {
            DeviceDiscoveryScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAction = { action -> navController.navigate(action) },
            )
        }

        composable<Route.RemoteLayout> {
            RemoteLayoutScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.RemoteBehavior> {
            RemoteBehaviorScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.ConnectionDisplay> {
            ConnectionDisplayScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.ConnectedDevices> {
            ConnectedDeviceScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.Faq> {
            val context = LocalContext.current
            FaqScreen(
                onNavigateBack = { navController.popBackStack() },
                onContactClick = {
                    sendFeedback(context, "RemoteLASTv - FAQ")
                },
            )
        }

        composable<Route.Language> {
            LanguageScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}