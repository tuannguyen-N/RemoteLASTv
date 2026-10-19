package com.tuannguyen.remotelastv.domain.model

data class RemoteSettings(
    val showMediaButtons: Boolean = false,
    val navigationMode: NavigationMode = NavigationMode.D_PAD,

    //Remote Behavior
    val isHapticFeedbackEnabled: Boolean = true,
    val isSmoothPressEnabled: Boolean = false,
    val isAutoShowKeyboardEnabled: Boolean = true,

    //Connection & Display
    val isAutoReconnectEnabled: Boolean = true,
    val isKeepScreenAwakeEnabled: Boolean = true,
    val isTheaterModeEnabled: Boolean = false,
)