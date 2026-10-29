package com.tuannguyen.remotelastv.ui.settings_remote_preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemotePreferencesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<RemotePreferencesUiState> = settingsRepository.remoteSettings
        .map {
            RemotePreferencesUiState(
                isAutoReconnectEnabled = it.isAutoReconnectEnabled,
                isKeepScreenAwakeEnabled = it.isKeepScreenAwakeEnabled,
                isTheaterModeEnabled = it.isTheaterModeEnabled,
                isHapticFeedbackEnabled = it.isHapticFeedbackEnabled,
                isSmoothPressEnabled = it.isSmoothPressEnabled,
                isAutoShowKeyboardEnabled = it.isAutoShowKeyboardEnabled
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RemotePreferencesUiState()
        )

    fun onHapticFeedbackChanged(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedbackEnabled(value)
        }
    }

    fun onSmoothPressChanged(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSmoothPressEnabled(value)
        }
    }

    fun onAutoShowKeyboardChanged(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoShowKeyboardEnabled(value)
        }
    }

    fun onAutoReconnectChanged(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoReconnectEnabled(value)
        }
    }

    fun onKeepScreenAwakeChanged(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setKeepScreenAwakeEnabled(value)
        }
    }

    fun onTheaterModeChanged(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTheaterModeEnabled(value)
        }
    }
}