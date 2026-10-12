package com.tuannguyen.remotelastv.domain.model

data class RemoteSettings(
    val showMediaButtons: Boolean = false,
    val navigationMode: NavigationMode = NavigationMode.D_PAD,
)