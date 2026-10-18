package com.tuannguyen.remotelastv.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            keyboardUiState = keyboardUiState,
            modifier = modifier,
        )
    }
}
