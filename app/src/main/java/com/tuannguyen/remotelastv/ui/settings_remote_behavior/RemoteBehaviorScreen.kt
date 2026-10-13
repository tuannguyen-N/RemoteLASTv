package com.tuannguyen.remotelastv.ui.settings_remote_behavior

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.ui.settings_remote_layout.RemoteLayoutContent
import com.tuannguyen.remotelastv.ui.settings_remote_layout.RemoteLayoutViewModel

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
    )
}