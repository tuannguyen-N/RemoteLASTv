package com.tuannguyen.remotelastv.domain.model

data class TutorialActions(
    val onNavigationModeChanged: (NavigationMode) -> Unit,
    val onMediaControlsChanged: (Boolean) -> Unit,
    val onAutoReconnectChanged: (Boolean) -> Unit,
    val onKeepScreenAwakeChanged: (Boolean) -> Unit,
    val onTheaterModeChanged: (Boolean) -> Unit,
    val onHapticFeedbackChanged: (Boolean) -> Unit,
    val onSmoothPressChanged: (Boolean) -> Unit,
    val onAutoShowKeyboardChanged: (Boolean) -> Unit,
    val onBack: () -> Unit,
    val onNext: () -> Unit,
    val onFinish: () -> Unit,
)