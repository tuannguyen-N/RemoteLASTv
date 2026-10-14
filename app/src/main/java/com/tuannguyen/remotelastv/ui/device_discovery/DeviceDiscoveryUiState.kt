package com.tuannguyen.remotelastv.ui.device_discovery

import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.DiscoveryStatus
import com.tuannguyen.remotelastv.domain.model.Television

data class DeviceDiscoveryUiState(
    val discoveryStatus: DiscoveryStatus = DiscoveryStatus.Scanning,
    val connectionState: ConnectionState = ConnectionState.Idle,
    val televisions: List<Television> = emptyList(),
) {
    val isEmpty: Boolean
        get() = discoveryStatus is DiscoveryStatus.Completed && televisions.isEmpty()
}