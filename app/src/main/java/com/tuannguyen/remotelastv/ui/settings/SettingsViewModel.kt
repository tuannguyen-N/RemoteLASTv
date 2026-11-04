package com.tuannguyen.remotelastv.ui.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.tuannguyen.remotelastv.data.ads.AdsConsentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val consentManager: AdsConsentManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(isPrivacyOptionsRequired = consentManager.privacyOptionsRequired)
    )
    val uiState = _uiState.asStateFlow()

    fun showPrivacyOptions(activity: Activity) {
        consentManager.showPrivacyOptions(activity)
    }
}