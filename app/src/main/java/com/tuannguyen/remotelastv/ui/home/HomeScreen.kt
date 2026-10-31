package com.tuannguyen.remotelastv.ui.home

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.domain.model.CastMediaType
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
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isTheaterModeActive = uiState.isTheaterModeEnabled && !uiState.isKeepScreenAwakeEnabled

    LaunchedEffect(Unit) {
        viewModel.effect.collect { event ->
            when (event) {
                is HomeEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
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
            remotePage = {
                RemotePage(connectionState = uiState.connectionState)
            },
            castPage = {
                CastPage(
                    connectionState = uiState.connectionState, openCast = openCast
                )
            },
            modifier = modifier,
        )
    }
}
