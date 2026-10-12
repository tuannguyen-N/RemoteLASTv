package com.tuannguyen.remotelastv.ui.settings_remote_layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.ui.settings.RemoteLayoutContent

@Composable
fun RemoteLayoutScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: RemoteLayoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RemoteLayoutContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigationModeChanged = viewModel::onNavigationModeChanged,
        onApplicationsChanged = {  },
        onColoredButtonsChanged = {  },
        onMediaButtonsChanged = viewModel::onShowMediaButtonsChanged,
        modifier = modifier
    )
}