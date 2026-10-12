package com.tuannguyen.remotelastv.ui.settings

import com.tuannguyen.remotelastv.ui.navigation.Route

data class SettingsUiState(
    val title: String = "Settings",
)

sealed interface SettingsAction {
    data class Navigate(val route: Route) : SettingsAction
    data object Close : SettingsAction
    data object Share : SettingsAction
    data object RateAndReview : SettingsAction
    data object Feedback : SettingsAction
    data object ActivatePremium : SettingsAction
}