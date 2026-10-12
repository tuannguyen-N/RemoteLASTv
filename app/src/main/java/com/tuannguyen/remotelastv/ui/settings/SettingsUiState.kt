package com.tuannguyen.remotelastv.ui.settings

data class SettingsUiState(
    val title: String = "Settings",
)

enum class SettingsAction {
    Close,
    RemoteLayout,
    RemoteBehavior,
    ManageApplications,
    ConnectedDevices,
    Faq,
    Share,
    RateAndReview,
    Feedback,
    Legal,
    ActivatePremium,
}