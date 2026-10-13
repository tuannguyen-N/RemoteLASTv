package com.tuannguyen.remotelastv.ui.device_discovery

import com.tuannguyen.remotelastv.domain.model.DiscoveryStatus
import com.tuannguyen.remotelastv.domain.model.Television

data class DeviceDiscoveryUiState(
    val status: DiscoveryStatus = DiscoveryStatus.Scanning,
    val televisions: List<Television> = emptyList(),
) {
    val isEmpty: Boolean
        get() = status is DiscoveryStatus.Completed && televisions.isEmpty()
}