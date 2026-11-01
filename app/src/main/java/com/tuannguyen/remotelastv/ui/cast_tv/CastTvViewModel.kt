package com.tuannguyen.remotelastv.ui.cast_tv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CastTvViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CastTvUiState())
    val uiState: StateFlow<CastTvUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<CastUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsRepository.remoteSettings.collect { settings ->
                _uiState.update {
                    it.copy(hasRequestedAudioPermission = settings.hasRequestedAudioPermission)
                }
            }
        }
    }

    fun onMediaTypeSelected(type: CastMediaType) {
        _uiState.update { it.copy(selectedMediaType = type) }
        _uiEvent.trySend(CastUiEvent.PickMedia(type))
    }

    fun onAudioPermissionRequestCompleted() {
        _uiState.update { it.copy(hasRequestedAudioPermission = true) }
        viewModelScope.launch {
            settingsRepository.setHasRequestedAudioPermission(true)
        }
    }
}
