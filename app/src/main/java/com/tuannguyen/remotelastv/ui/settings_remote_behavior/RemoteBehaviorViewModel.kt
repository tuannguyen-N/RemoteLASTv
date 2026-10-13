package com.tuannguyen.remotelastv.ui.settings_remote_behavior

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import com.tuannguyen.remotelastv.ui.settings_remote_layout.RemoteLayoutUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteBehaviorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<RemoteBehaviorUiState> = settingsRepository.remoteSettings
        .map {
            RemoteBehaviorUiState(
                isHapticFeedbackEnabled = it.isHapticFeedbackEnabled,
                isSmoothPressEnabled = it.isSmoothPressEnabled
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RemoteBehaviorUiState()
        )

    fun onHapticFeedbackChanged(value: Boolean){
        viewModelScope.launch {
           settingsRepository.setHapticFeedbackEnabled(value)
        }
    }

    fun onSmoothPressChanged(value: Boolean){
        viewModelScope.launch {
            settingsRepository.setSmoothPressEnabled(value)
        }
    }
}