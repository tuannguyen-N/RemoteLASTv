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
    var failureMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.startScan()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DiscoveryEvent.ConnectionSuccess -> {
                    successTvName = event.tvName
                }

                is DiscoveryEvent.ConnectionFailure -> {
                    failureMessage = event.message
                }
            }
        }
    }

    DeviceDiscoveryContent(
        uiState = uiState,
        successTvName = successTvName,
        onClose = onNavigateBack,
        onRetry = viewModel::onRetry,
        onTelevisionClick = viewModel::connect,
        onReturnHome = onNavigateBack,
        onDisconnectClick = viewModel::disconnect,
        failureMessage = failureMessage,
        onDismissFailure = { failureMessage = null }
    )
}