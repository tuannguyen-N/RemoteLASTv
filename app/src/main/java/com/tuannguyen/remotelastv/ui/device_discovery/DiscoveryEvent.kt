package com.tuannguyen.remotelastv.ui.device_discovery

sealed interface DiscoveryEvent {
    data class ConnectionSuccess(val tvName: String) : DiscoveryEvent
}