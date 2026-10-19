package com.tuannguyen.remotelastv.ui.home

import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.VolumeState


data class HomeUiState(
    val connectionState: ConnectionState = ConnectionState.Idle,
    val selectedPage: Int = 0,
    val channel: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val showMediaButtons: Boolean = false,
    val navigationMode: NavigationMode = NavigationMode.D_PAD,
    val isKeepScreenAwakeEnabled: Boolean = true,
    val isTheaterModeEnabled: Boolean = false,
    val isHapticFeedbackEnabled: Boolean = true,

    val shortcutApps: List<KnownApp> = emptyList(),
    val availableApps: List<KnownApp> = emptyList(),

    val touchpadMode: TouchpadMode = TouchpadMode.DIRECTIONAL,
    val volume: VolumeState? = null,
    val capabilities: Set<TvCapability> = emptySet(),
) {
    val supportsVoice: Boolean get() = TvCapability.VOICE in capabilities
}

sealed interface HomeEvent {
    data class ShowMessage(val message: String) : HomeEvent
}
