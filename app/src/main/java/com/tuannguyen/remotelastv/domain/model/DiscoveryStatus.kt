package com.tuannguyen.remotelastv.domain.model

sealed interface DiscoveryStatus {
    data object Scanning : DiscoveryStatus
    data object Completed : DiscoveryStatus
    data class Error(val message: String) : DiscoveryStatus
}