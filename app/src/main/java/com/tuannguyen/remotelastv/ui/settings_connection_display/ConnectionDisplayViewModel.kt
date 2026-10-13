package com.tuannguyen.remotelastv.ui.settings_connection_display

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
class ConnectionDisplayViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<ConnectionDisplayUiState> = settingsRepository.remoteSettings
        .map {
            ConnectionDisplayUiState(
                isAutoReconnectEnabled = it.isAutoReconnectEnabled,
                isKeepScreenAwakeEnabled = it.isKeepScreenAwakeEnabled,
                isTheaterModeEnabled = it.isTheaterModeEnabled,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectionDisplayUiState()
        )

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
