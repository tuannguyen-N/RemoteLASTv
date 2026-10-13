package com.tuannguyen.remotelastv.ui.device_discovery

import com.tuannguyen.remotelastv.domain.model.Television

sealed interface DeviceDiscoveryUiState {

    data class Scanning(
        val televisions: List<Television> = emptyList(),
    ) : DeviceDiscoveryUiState

    data object Empty : DeviceDiscoveryUiState

    data class Error(
        val message: String,
    ) : DeviceDiscoveryUiState

    data class Success(
        val televisions: List<Television>,
    ) : DeviceDiscoveryUiState
}