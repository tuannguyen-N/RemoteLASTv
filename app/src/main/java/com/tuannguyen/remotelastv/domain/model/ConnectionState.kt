package com.tuannguyen.remotelastv.domain.model

sealed interface ConnectionState {
    data object Idle : ConnectionState
    data object Disconnected : ConnectionState
    data class Connecting(val tv: Television) : ConnectionState
    data class WaitingForPin(val tv: Television) : ConnectionState
    data class WaitingForAuth(val tv: Television) : ConnectionState
    data class Connected(val tv: Television) : ConnectionState
    data class TempDisconnected(val tv: Television) : ConnectionState
    data class Reconnecting(val tv: Television, val attempt: Int) : ConnectionState
}