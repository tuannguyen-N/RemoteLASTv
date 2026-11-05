package com.tuannguyen.remotelastv.ui.remote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TutorialActions
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.ui.remote.components.FirstConnectTutorialDialog
import com.tuannguyen.remotelastv.ui.remote.components.KeyboardInputBottomSheet
import com.tuannguyen.remotelastv.ui.remote.components.RemoteControls
import com.tuannguyen.remotelastv.ui.utils.findActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun RemotePage(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
    onUserInteraction: () -> Unit = {},
    onRemoteReadyChanged: (Boolean) -> Unit = {},
    viewModel: RemoteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val notConnectedMessage = stringResource(R.string.remote_not_connected_message)
    var tutorialStep by remember { mutableIntStateOf(0) }
    val connectedTvId = (connectionState as? ConnectionState.Connected)?.tv?.id
    var touchpadModeOverride by remember(connectedTvId) {
        mutableStateOf<TouchpadMode?>(null)
    }
    val touchpadMode = touchpadModeOverride ?: uiState.touchpadMode
    val connectedTvBrand = (connectionState as? ConnectionState.Connected)?.tv?.brand
    val showMouseToggle = when (connectedTvBrand) {
        TelevisionBrand.SAMSUNG -> true
        TelevisionBrand.LG -> uiState.touchpadMode == TouchpadMode.POINTER
        else -> false
    }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onMicrophonePermissionRequested()
        if (granted) {
            viewModel.startVoiceRecording()
        } else if (uiState.hasRequestedMicrophonePermission) {
            viewModel.onMicrophonePermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        var snackbarJob: Job? = null
        viewModel.effect.collect { event ->
            val message = when (event) {
                is RemoteEvent.ShowMessage -> event.message
                RemoteEvent.NotConnected -> notConnectedMessage
            }
            snackbarJob?.cancel()
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarJob = this@LaunchedEffect.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val pagerState = rememberPagerState(
        initialPage = uiState.selectedPage,
        pageCount = { 2 },
    )
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageSelected(pagerState.currentPage)
    }
    LaunchedEffect(uiState.selectedPage) {
        if (!pagerState.isScrollInProgress && pagerState.settledPage != uiState.selectedPage) {
            pagerState.animateScrollToPage(uiState.selectedPage)
        }
    }

    val isTutorialVisible =
        connectionState is ConnectionState.Connected && !uiState.hasSeenRemoteTutorial

    LaunchedEffect(isTutorialVisible, uiState.keyboard.showKeyboardSheet) {
        onRemoteReadyChanged(!isTutorialVisible && !uiState.keyboard.showKeyboardSheet)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = this.maxHeight < 700.dp
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                RemoteControls(
                    uiState = uiState,
                    pagerState = pagerState,
                    compact = compact,
                    onRemoteButtonClick = { key ->
                        onUserInteraction()
                        viewModel.onRemoteButtonClick(key)
                    },
                    touchpadMode = touchpadMode,
                    touchpadEvent = { event ->
                        onUserInteraction()
                        viewModel.onTouchpad(event)
                    },
                    showMouseToggle = showMouseToggle,
                    onMouseToggle = {
                        onUserInteraction()
                        touchpadModeOverride =
                            if (touchpadMode == TouchpadMode.POINTER) {
                                TouchpadMode.DIRECTIONAL
                            } else {
                                TouchpadMode.POINTER
                            }
                    },
                    onPageSelected = { page ->
                        if (page != uiState.selectedPage) onUserInteraction()
                        viewModel.onPageSelected(page)
                    },
                    showKeyboardSheet = {
                        onUserInteraction()
                        viewModel.showKeyboardSheet()
                    },
                    onStartVoiceRecording = {
                        onUserInteraction()
                        if (
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO,
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.startVoiceRecording()
                        } else if (
                            uiState.hasRequestedMicrophonePermission &&
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
                    onStopVoiceRecording = {
                        onUserInteraction()
                        viewModel.stopVoiceRecording()
                    },
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
            )
        }
    }

    if (uiState.keyboard.showKeyboardSheet) {
        KeyboardInputBottomSheet(
            query = uiState.keyboard.query,
            inputHint = uiState.keyboard.inputHint,
            onDismiss = viewModel::dismissKeyboardSheet,
            onSearch = {
                onUserInteraction()
                viewModel.dismissKeyboardSheet()
                viewModel.onEnter()
            },
            onQueryChange = {
                onUserInteraction()
                viewModel.onQueryChange(it)
            },
        )
    }

    if (isTutorialVisible) {
        FirstConnectTutorialDialog(
            step = tutorialStep,
            uiState = uiState,
            actions = TutorialActions(
                onNavigationModeChanged = viewModel::onTutorialNavigationModeChanged,
                onMediaControlsChanged = viewModel::onTutorialMediaControlsChanged,
                onAutoReconnectChanged = viewModel::onTutorialAutoReconnectChanged,
                onKeepScreenAwakeChanged = viewModel::onTutorialKeepScreenAwakeChanged,
                onTheaterModeChanged = viewModel::onTutorialTheaterModeChanged,
                onHapticFeedbackChanged = viewModel::onTutorialHapticFeedbackChanged,
                onSmoothPressChanged = viewModel::onTutorialSmoothPressChanged,
                onAutoShowKeyboardChanged = viewModel::onTutorialAutoShowKeyboardChanged,
                onBack = { if (tutorialStep > 0) tutorialStep-- },
                onNext = {
                    if (tutorialStep < 2) tutorialStep++
                },
                onFinish = {
                    tutorialStep = 0
                    viewModel.onTutorialFinished()
                },
            ),
            showMouseFeaturesHint = showMouseToggle,
        )
    }
}
