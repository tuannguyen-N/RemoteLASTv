package com.tuannguyen.remotelastv.ui.premium

import androidx.annotation.StringRes

sealed interface PremiumUiEvent {
    data object Close : PremiumUiEvent
    data class ShowMessage(@StringRes val messageRes: Int) : PremiumUiEvent
}