package com.tuannguyen.remotelastv.ui.settings_remote_behavior

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RemoteBehaviorScreen(
    onNavigateBack: () -> Unit,
    viewModel: RemoteBehaviorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RemoteBehaviorContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onHapticFeedbackChanged = viewModel::onHapticFeedbackChanged,
        onSmoothPressChanged = viewModel::onSmoothPressChanged,
        onAutoShowKeyboardChanged = viewModel::onAutoShowKeyboardChanged
    )
}