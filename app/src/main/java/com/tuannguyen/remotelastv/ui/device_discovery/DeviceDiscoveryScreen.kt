package com.tuannguyen.remotelastv.ui.device_discovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.ui.device_discovery.components.DeviceDiscoveryContent

@Composable
fun DeviceDiscoveryScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeviceDiscoveryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var successTvName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.startScan()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DiscoveryEvent.ConnectionSuccess -> {
                    successTvName = event.television.name
                }
            }
        }
    }

    DeviceDiscoveryContent(
        uiState = uiState,
        onClose = onNavigateBack,
        onRetry = viewModel::onRetry,
        onTelevisionClick = viewModel::connect,
        successTvName = successTvName,
        onReturnHome = onNavigateBack,
        onDisconnectClick = viewModel::disconnect,
        onDismissFailure = viewModel::acknowledgeConnectionFailure,
        onSubmitPin = viewModel::submitPin,
        onCancelSendPin = viewModel::cancelSendPin,
    )
}