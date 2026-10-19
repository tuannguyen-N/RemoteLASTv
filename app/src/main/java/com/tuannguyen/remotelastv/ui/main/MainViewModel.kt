package com.tuannguyen.remotelastv.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {
    val isHapticEnabled: StateFlow<Boolean> =
        settingsRepository.remoteSettings.map { it.isHapticFeedbackEnabled }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isSmoothPressEnabled: StateFlow<Boolean> =
        settingsRepository.remoteSettings.map { it.isSmoothPressEnabled }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)
}