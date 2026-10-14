package com.tuannguyen.remotelastv.domain.model

sealed interface ConnectionStatus {
    data object Connecting : ConnectionStatus
    data object WaitingForPin : ConnectionStatus
    data object WaitingForAuth : ConnectionStatus
    data class Connected(val credentials: TvCredentials) : ConnectionStatus
    data class Disconnected(val reason: String? = null, val recoverable: Boolean = true) : ConnectionStatus
    data class Failed(val reason: FailReason) : ConnectionStatus
}