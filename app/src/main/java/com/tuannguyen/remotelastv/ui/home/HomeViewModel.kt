package com.tuannguyen.remotelastv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.controller.TvControllerManager
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.model.TvApp
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.repository.SavedTvRepository
import com.tuannguyen.remotelastv.domain.usecase.ReconnectLastTvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val controllerManager: TvControllerManager,
    private val connectionManager: TvConnectionManager,
    private val reconnectLastTvUseCase: ReconnectLastTvUseCase,
) : ViewModel() {

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    private val _localState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = combine(
        _localState,
        settingsRepository.remoteSettings,
        connectionManager.state
    ) { local, settings, connectionState ->
        local.copy(
            showMediaButtons = settings.showMediaButtons,
            navigationMode = settings.navigationMode,
            connectionState = connectionState,
            isKeepScreenAwakeEnabled = settings.isKeepScreenAwakeEnabled,
            isTheaterModeEnabled = settings.isTheaterModeEnabled,
            isHapticFeedbackEnabled = settings.isHapticFeedbackEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        autoReconnect()
    }

    private fun autoReconnect() {
        viewModelScope.launch {
            val result = reconnectLastTvUseCase()
            if (result is ConnectionState.Connected) {
                _effect.send(HomeEffect.ShowMessage("Connected to ${result.tv.name}"))
            }
        }
    }

    fun onPowerClick() = send { it.sendKey(TvKey.POWER) }
    fun onRemoteButtonClick(key: TvKey) = send { it.sendKey(key) }
    fun onAppClick(channel: TvApp) = send { it.launchApp(channel) }

    fun onPageSelected(page: Int) {
        _localState.update { it.copy(selectedPage = page) }
    }

    private fun send(block: suspend (TvControllerRepository) -> Result<Unit>) {
        viewModelScope.launch {
            controllerManager.execute(block).onFailure { e ->
                if (e !is TvException.NotConnected) {
//                    _events.send(UiEvent.ShowError(e))
                }
            }
        }
    }
}
