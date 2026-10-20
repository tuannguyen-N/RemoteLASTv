package com.tuannguyen.remotelastv.ui.device_discovery

import com.tuannguyen.remotelastv.domain.model.Television

sealed interface DiscoveryEvent {
    data class ConnectionSuccess(val television: Television) : DiscoveryEvent
}