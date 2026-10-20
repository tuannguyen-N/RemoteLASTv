package com.tuannguyen.remotelastv.ui.settings_remote_layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.ui.settings_remote_layout.components.RemoteLayoutContent

@Composable
fun RemoteLayoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: RemoteLayoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RemoteLayoutContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigationModeChanged = viewModel::onNavigationModeChanged,
        onMediaControlsChanged = viewModel::onShowMediaButtonsChanged,
    )
}