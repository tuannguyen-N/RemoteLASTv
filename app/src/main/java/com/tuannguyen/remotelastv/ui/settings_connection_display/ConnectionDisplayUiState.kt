package com.tuannguyen.remotelastv.ui.settings_connection_display

data class ConnectionDisplayUiState(
    val isAutoReconnectEnabled: Boolean = false,
    val isKeepScreenAwakeEnabled: Boolean = false,
    val isTheaterModeEnabled: Boolean = false,
)
