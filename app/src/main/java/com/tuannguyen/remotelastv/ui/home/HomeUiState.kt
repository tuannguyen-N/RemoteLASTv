package com.tuannguyen.remotelastv.ui.home

import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.KnownApp

data class HomeUiState(
    val connectionState: ConnectionState = ConnectionState.Idle,
    val shortcutApps: List<KnownApp> = emptyList(),
    val availableApps: List<KnownApp> = emptyList(),
    val isKeepScreenAwakeEnabled: Boolean = true,
    val isTheaterModeEnabled: Boolean = false,
)

sealed interface HomeEvent {
    data class ShowMessage(val message: String) : HomeEvent
}
