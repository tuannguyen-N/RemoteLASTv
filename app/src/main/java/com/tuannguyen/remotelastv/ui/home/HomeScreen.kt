package com.tuannguyen.remotelastv.ui.home

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.ui.ads.interstitial.CastInterstitialViewModel
import com.tuannguyen.remotelastv.ui.cast_tv.CastPage
import com.tuannguyen.remotelastv.ui.effects.KeepScreenOnEffect
import com.tuannguyen.remotelastv.ui.effects.TheaterModeContainer
import com.tuannguyen.remotelastv.ui.home.components.HomeContent
import com.tuannguyen.remotelastv.ui.remote.RemotePage

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDeviceDiscovery: () -> Unit = {},
    openCast: (CastMediaType, Uri) -> Unit,
    openAudio: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val interstitialViewModel: CastInterstitialViewModel = hiltViewModel()
    val isTheaterModeActive = uiState.isTheaterModeEnabled && !uiState.isKeepScreenAwakeEnabled

    LaunchedEffect(Unit) {
        viewModel.effect.collect { event ->
            when (event) {
                is HomeEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                HomeEvent.ShowInterstitial -> {
                    if (
                        !viewModel.canShowHomeInterstitialNow() ||
                        !interstitialViewModel.showIfReady(
                            context = context,
                            onFinished = viewModel::onInterstitialFinished,
                        )
                    ) {
                        viewModel.onInterstitialUnavailable()
                    }
                }
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onHomeResumed()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onHomePaused()
    }

    DisposableEffect(viewModel) {
        onDispose(viewModel::onHomePaused)
    }

    KeepScreenOnEffect(enabled = uiState.isKeepScreenAwakeEnabled)

    TheaterModeContainer(enabled = isTheaterModeActive) {
        HomeContent(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onSettingsClick = onNavigateToSettings,
            onChangeTelevision = onNavigateToDeviceDiscovery,
            onPowerClick = viewModel::onPowerClick,
            onAppClick = viewModel::onAppClick,
            onUserInteraction = viewModel::onRemoteInteraction,
            remotePage = {
                RemotePage(
                    connectionState = uiState.connectionState,
                    onUserInteraction = viewModel::onRemoteInteraction,
                    onRemoteReadyChanged = viewModel::onRemotePageReadyChanged,
                )
            },
            castPage = {
                CastPage(
                    connectionState = uiState.connectionState,
                    openCast = openCast,
                    openAudio = openAudio,
                )
            },
            onRemotePageVisibilityChanged = viewModel::onRemotePageVisibilityChanged,
            modifier = modifier,
        )
    }
}
