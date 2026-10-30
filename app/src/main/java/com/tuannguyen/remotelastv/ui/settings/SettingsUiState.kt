package com.tuannguyen.remotelastv.ui.settings

import androidx.annotation.StringRes
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.navigation.Route

data class SettingsUiState(
    @param:StringRes val titleRes: Int = R.string.settings_title,
)

sealed interface SettingsAction {
    data class Navigate(val route: Route) : SettingsAction
    data object Close : SettingsAction
    data object Share : SettingsAction
    data object RateAndReview : SettingsAction
    data object Feedback : SettingsAction
    data object ActivatePremium : SettingsAction
    data object PrivacyPolicy : SettingsAction
}