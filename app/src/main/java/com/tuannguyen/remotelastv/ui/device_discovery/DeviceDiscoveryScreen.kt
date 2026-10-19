package com.tuannguyen.remotelastv.ui.device_discovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.ui.device_discovery.components.DeviceDiscoveryContent

@Composable
fun DeviceDiscoveryScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeviceDiscoveryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.startScan()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
//            when (event) {
//                is DiscoveryEvent.ConnectionSuccess -> {
//
//                }
//            }
        }
    }

    DeviceDiscoveryContent(
        uiState = uiState,
        onClose = onNavigateBack,
        onRetry = viewModel::onRetry,
        onTelevisionClick = viewModel::connect,
        onReturnHome = onNavigateBack,
        onDisconnectClick = viewModel::disconnect,
        onDismissFailure = viewModel::acknowledgeConnectionFailure,
        onSubmitPin = viewModel::submitPin,
        onCancelSendPin = viewModel::cancelSendPin,
    )
}