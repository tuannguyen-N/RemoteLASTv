package com.tuannguyen.remotelastv.ui.settings_remote_preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RemotePreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: RemotePreferencesViewModel = hiltViewModel()
) {
    val preferencesUiState by viewModel.uiState.collectAsStateWithLifecycle()

    RemotePreferencesContent(
        preferencesUiState = preferencesUiState,
        onNavigateBack = onNavigateBack,
        onHapticFeedbackChanged = viewModel::onHapticFeedbackChanged,
        onSmoothPressChanged = viewModel::onSmoothPressChanged,
        onAutoShowKeyboardChanged = viewModel::onAutoShowKeyboardChanged,
        onAutoReconnectChanged = viewModel::onAutoReconnectChanged,
        onKeepScreenAwakeChanged = viewModel::onKeepScreenAwakeChanged,
        onTheaterModeChanged = viewModel::onTheaterModeChanged
    )
}