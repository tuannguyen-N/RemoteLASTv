package com.tuannguyen.remotelastv.ui.device_discovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DeviceDiscoveryScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeviceDiscoveryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel){
        viewModel.startScan()
    }

    DeviceDiscoveryContent(
        uiState = uiState,
        onClose = onNavigateBack,
        onRetry = viewModel::onRetry,
        onTelevisionClick = {
            // TODO: connect
        },
    )
}