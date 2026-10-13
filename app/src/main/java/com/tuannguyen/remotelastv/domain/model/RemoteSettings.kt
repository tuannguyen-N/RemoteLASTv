package com.tuannguyen.remotelastv.domain.model

data class RemoteSettings(
    val showMediaButtons: Boolean = false,
    val navigationMode: NavigationMode = NavigationMode.D_PAD,

    //Remote Behavior
    val isHapticFeedbackEnabled: Boolean = false,
    val isSmoothPressEnabled: Boolean = false,

    //Connection & Display
    val isAutoReconnectEnabled: Boolean = false,
    val isKeepScreenAwakeEnabled: Boolean = false,
    val isTheaterModeEnabled: Boolean = false,
)