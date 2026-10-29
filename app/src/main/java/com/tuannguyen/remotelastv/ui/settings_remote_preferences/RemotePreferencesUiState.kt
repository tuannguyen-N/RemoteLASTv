package com.tuannguyen.remotelastv.ui.settings_remote_preferences

data class RemotePreferencesUiState(
    val isAutoReconnectEnabled: Boolean = false,
    val isKeepScreenAwakeEnabled: Boolean = false,
    val isTheaterModeEnabled: Boolean = false,
    val isHapticFeedbackEnabled: Boolean = false,
    val isSmoothPressEnabled: Boolean = false,
    val isAutoShowKeyboardEnabled: Boolean = false
)