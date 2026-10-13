package com.tuannguyen.remotelastv.ui.settings_connection_display

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConnectionDisplayScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConnectionDisplayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ConnectionDisplayContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onAutoReconnectChanged = viewModel::onAutoReconnectChanged,
        onKeepScreenAwakeChanged = viewModel::onKeepScreenAwakeChanged,
        onTheaterModeChanged = viewModel::onTheaterModeChanged,
    )
}
