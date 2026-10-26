package com.tuannguyen.remotelastv.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.domain.model.TutorialActions
import com.tuannguyen.remotelastv.ui.effects.KeepScreenOnEffect
import com.tuannguyen.remotelastv.ui.effects.TheaterModeContainer
import com.tuannguyen.remotelastv.ui.home.components.HomeContent

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    keyboardViewModel: KeyboardViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDeviceDiscovery: () -> Unit = {},

    ) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val keyboardUiState by keyboardViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        homeViewModel.onMicrophonePermissionRequested()
        if (granted) {
            homeViewModel.startVoiceRecording()
        } else {
            if (homeUiState.hasRequestedMicrophonePermission) {
                homeViewModel.onMicrophonePermissionDenied()
            }
        }
    }
    val isTheaterModeActive =
        homeUiState.isTheaterModeEnabled && !homeUiState.isKeepScreenAwakeEnabled

    LaunchedEffect(Unit) {
        homeViewModel.effect.collect { effect ->
            when (effect) {
                is HomeEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    KeepScreenOnEffect(
        enabled = homeUiState.isKeepScreenAwakeEnabled
    )

    TheaterModeContainer(enabled = isTheaterModeActive) {
        HomeContent(
            homeUiState = homeUiState,
            snackbarHostState = snackbarHostState,
            onSettingsClick = {
                onNavigateToSettings()
            },
            onChangeTelevision = onNavigateToDeviceDiscovery,
            onPowerClick = homeViewModel::onPowerClick,
            onAppClick = homeViewModel::onAppClick,
            onRemoteButtonClick = homeViewModel::onRemoteButtonClick,
            onPageSelected = homeViewModel::onPageSelected,
            touchpadMode = homeUiState.touchpadMode,
            touchpadEvent = homeViewModel::onTouchpad,
            onShowKeyboardSheet = keyboardViewModel::showKeyboardSheet,
            onDismissKeyboardSheet = keyboardViewModel::dismissKeyboardSheet,
            onQueryChange = keyboardViewModel::onQueryChange,
            onKeyboardSubmit = keyboardViewModel::onEnter,
            onStartVoiceRecording = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    homeViewModel.startVoiceRecording()
                } else if (
                    homeUiState.hasRequestedMicrophonePermission &&
                    activity != null &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.RECORD_AUDIO,
                    )
                ) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        },
                    )
                } else {
                    microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onStopVoiceRecording = homeViewModel::stopVoiceRecording,
            tutorialActions = TutorialActions(
                onNavigationModeChanged = homeViewModel::onTutorialNavigationModeChanged,
                onMediaControlsChanged = homeViewModel::onTutorialMediaControlsChanged,
                onAutoReconnectChanged = homeViewModel::onTutorialAutoReconnectChanged,
                onKeepScreenAwakeChanged = homeViewModel::onTutorialKeepScreenAwakeChanged,
                onTheaterModeChanged = homeViewModel::onTutorialTheaterModeChanged,
                onHapticFeedbackChanged = homeViewModel::onTutorialHapticFeedbackChanged,
                onSmoothPressChanged = homeViewModel::onTutorialSmoothPressChanged,
                onAutoShowKeyboardChanged = homeViewModel::onTutorialAutoShowKeyboardChanged,
                onBack = {},
                onNext = {},
                onFinish = homeViewModel::onTutorialFinished,
            ),
            keyboardUiState = keyboardUiState,
            modifier = modifier,
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
