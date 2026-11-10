package com.tuannguyen.remotelastv.ui.navigation

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
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.ui.cast_audio.CastAudioScreen
import com.tuannguyen.remotelastv.ui.cast_image.CastImageScreen
import com.tuannguyen.remotelastv.ui.cast_video.CastVideoScreen
import com.tuannguyen.remotelastv.ui.device_discovery.DeviceDiscoveryScreen
import com.tuannguyen.remotelastv.ui.faq.FaqScreen
import com.tuannguyen.remotelastv.ui.home.HomeScreen
import com.tuannguyen.remotelastv.ui.language.LanguageScreen
import com.tuannguyen.remotelastv.ui.premium.PremiumScreen
import com.tuannguyen.remotelastv.ui.settings.SettingsScreen
import com.tuannguyen.remotelastv.ui.settings_connected_device.ConnectedDeviceScreen
import com.tuannguyen.remotelastv.ui.settings_remote_layout.RemoteLayoutScreen
import com.tuannguyen.remotelastv.ui.settings_remote_preferences.RemotePreferencesScreen
import com.tuannguyen.remotelastv.ui.utils.openPrivacyPolicy
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
    ) {
        composable<Route.Home> {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Route.Settings)
                },
                onNavigateToDeviceDiscovery = {
                    navController.navigate(Route.DeviceDiscovery)
                },
                openCast = { castType, uri ->
                    when (castType) {
                        CastMediaType.IMAGE -> navController.navigate(Route.CastImage(uri.toString()))
                        CastMediaType.VIDEO -> navController.navigate(Route.CastVideo(uri.toString()))
                        CastMediaType.AUDIO -> navController.navigate(Route.CastAudio)
                    }
                },
                openAudio = {
                    navController.navigate(Route.CastAudio)
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

        composable<Route.Premium> {
            val context = LocalContext.current
            PremiumScreen(
                onClose = { navController.popBackStack() },
                onSubscribe = {},
                onTermsClick = {},
                onPrivacyClick = {
                    openPrivacyPolicy(context)
                },
            )
        }

        composable<Route.RemoteLayout> {
            RemoteLayoutScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.RemotePreferences> {
            RemotePreferencesScreen(
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
                    sendFeedback(context, R.string.faq_feedback_subject)
                },
            )
        }

        composable<Route.Language> {
            LanguageScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.CastVideo> {
            CastVideoScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.CastImage> {
            CastImageScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Route.CastAudio> {
            CastAudioScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}