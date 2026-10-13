package com.tuannguyen.remotelastv.ui.device_discovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tuannguyen.remotelastv.domain.model.Television

@Composable
fun DeviceDiscoveryScreen(
    onClose: () -> Unit,
    onTelevisionClick: (Television) -> Unit,
    viewModel: DeviceDiscoveryViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DeviceDiscoveryContent(
        uiState = uiState,
        onClose = onClose,
        onRetry = viewModel::onRetry,
        onTelevisionClick = onTelevisionClick,
    )
}