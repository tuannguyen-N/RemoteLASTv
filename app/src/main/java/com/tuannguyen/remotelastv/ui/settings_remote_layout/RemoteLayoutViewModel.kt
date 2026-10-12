package com.tuannguyen.remotelastv.ui.settings_remote_layout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteLayoutViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<RemoteLayoutUiState> = settingsRepository.remoteSettings
        .map {
            RemoteLayoutUiState(
                navigationMode = it.navigationMode,
                showMediaButtons = it.showMediaButtons
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RemoteLayoutUiState()
        )

    fun onNavigationModeChanged(mode: NavigationMode) {
        viewModelScope.launch {
            settingsRepository.setNavigationMode(mode)
        }
    }

    fun onShowMediaButtonsChanged(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowMediaButtons(show)
        }
    }
}