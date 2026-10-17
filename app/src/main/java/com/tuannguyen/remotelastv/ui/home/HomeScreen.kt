package com.tuannguyen.remotelastv.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tuannguyen.remotelastv.ui.effects.KeepScreenOnEffect
import com.tuannguyen.remotelastv.ui.effects.TheaterModeContainer

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDeviceDiscovery: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isTheaterModeActive = uiState.isTheaterModeEnabled && !uiState.isKeepScreenAwakeEnabled

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    KeepScreenOnEffect(
        enabled = uiState.isKeepScreenAwakeEnabled
    )

    TheaterModeContainer(enabled = isTheaterModeActive) {
        HomeContent(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onSettingsClick = {
                onNavigateToSettings()
            },
            onChangeTelevision = onNavigateToDeviceDiscovery,
            onPowerClick = viewModel::onPowerClick,
            onAppClick = viewModel::onAppClick,
            onRemoteButtonClick = viewModel::onRemoteButtonClick,
            onPageSelected = viewModel::onPageSelected,
            touchpadMode = uiState.touchpadMode,
            touchpadEvent = viewModel::onTouchpad,
            modifier = modifier,
        )
    }
}
