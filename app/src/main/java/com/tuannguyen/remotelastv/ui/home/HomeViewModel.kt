package com.tuannguyen.remotelastv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.model.RemoteKey
import com.tuannguyen.remotelastv.domain.model.TvApp
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class HomeViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _localState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = combine(
        _localState,
        settingsRepository.remoteSettings
    ) { local, settings ->
        local.copy(
            showMediaButtons = settings.showMediaButtons,
            navigationMode = settings.navigationMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onPowerClick() {
        // TODO: Send power command to the connected TV.
        _effects.trySend(HomeEffect.ShowMessage("Power control is not connected yet"))
    }

    fun onSettingsClick() {
        _effects.trySend(HomeEffect.ShowMessage("Settings screen is not implemented yet"))
    }

    fun onDeviceClick() {
        _effects.trySend(HomeEffect.ShowMessage("Device selector is not implemented yet"))
    }

    fun onAppClick(appName: TvApp) {
        // TODO: Implement launching the selected TV app.
        _effects.trySend(HomeEffect.ShowMessage("$appName control is not connected yet"))
    }

    fun onRemoteButtonClick(button: RemoteKey) {
        // TODO: Send remote-control command.
        _effects.trySend(HomeEffect.ShowMessage("$button command is not connected yet"))
    }

    fun onPageSelected(page: Int) {
        _localState.update { it.copy(selectedPage = page) }
    }

    fun onMuteToggled() {
//        _localState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun onRetry() {
        _localState.update { it.copy(isLoading = false, errorMessage = null, isEmpty = false) }
    }
}
