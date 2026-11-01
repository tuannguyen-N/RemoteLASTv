package com.tuannguyen.remotelastv.ui.remote

import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.VolumeState

data class RemoteUiState(
    val selectedPage: Int = 0,
    val showMediaButtons: Boolean = false,
    val navigationMode: NavigationMode = NavigationMode.D_PAD,
    val isAutoReconnectEnabled: Boolean = true,
    val isKeepScreenAwakeEnabled: Boolean = true,
    val isTheaterModeEnabled: Boolean = false,
    val isHapticFeedbackEnabled: Boolean = true,
    val isSmoothPressEnabled: Boolean = false,
    val isAutoShowKeyboardEnabled: Boolean = true,
    val hasSeenRemoteTutorial: Boolean = false,
    val hasRequestedMicrophonePermission: Boolean = false,
    val isRecording: Boolean = false,
    val touchpadMode: TouchpadMode = TouchpadMode.DIRECTIONAL,
    val volume: VolumeState? = null,
    val capabilities: Set<TvCapability> = setOf(),
    val keyboard: KeyboardUiState = KeyboardUiState(),
) {
    val supportsVoice: Boolean get() = TvCapability.VOICE in capabilities
}

sealed interface RemoteEvent {
    data class ShowMessage(val message: String) : RemoteEvent
    data object NotConnected : RemoteEvent
}
