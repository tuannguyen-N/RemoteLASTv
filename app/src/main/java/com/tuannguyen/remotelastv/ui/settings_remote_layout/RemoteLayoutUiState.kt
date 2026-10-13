package com.tuannguyen.remotelastv.ui.settings_remote_layout

import com.tuannguyen.remotelastv.domain.model.NavigationMode

data class RemoteLayoutUiState(
    val navigationMode: NavigationMode = NavigationMode.D_PAD,
    val showMediaButtons: Boolean = false
)