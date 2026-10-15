package com.tuannguyen.remotelastv.domain.model

data class RemoteSettings(
    val showMediaButtons: Boolean = false,
    val navigationMode: NavigationMode = NavigationMode.D_PAD,

    //Remote Behavior
    val isHapticFeedbackEnabled: Boolean = false,
    val isSmoothPressEnabled: Boolean = false,

    //Connection & Display
    val isAutoReconnectEnabled: Boolean = true,
    val isKeepScreenAwakeEnabled: Boolean = true,
    val isTheaterModeEnabled: Boolean = false,
)