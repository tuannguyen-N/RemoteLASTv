package com.tuannguyen.remotelastv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.controller.TvControllerManager
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TouchpadEvent
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import com.tuannguyen.remotelastv.domain.usecase.GetTvAppsUseCase
import com.tuannguyen.remotelastv.domain.usecase.ReconnectLastTvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
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
    private val getTvAppsUseCase: GetTvAppsUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val supportedApps: Flow<List<KnownApp>?> = connectionManager.state
        .map { (it as? ConnectionState.Connected)?.tv }
        .distinctUntilChanged()
        .mapLatest { tv -> tv?.let { getTvAppsUseCase().getOrNull() } }

    private val _effect = Channel<HomeEvent>(Channel.BUFFERED)
    val effect: Flow<HomeEvent> = _effect.receiveAsFlow()

    private val _localState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = combine(
        _localState,
        settingsRepository.remoteSettings,
        connectionManager.state,
        supportedApps,
        controllerManager.touchpadMode
    ) { local, settings, connectionState, supported, touchpadMode ->
        local.copy(
            showMediaButtons = settings.showMediaButtons,
            navigationMode = settings.navigationMode,
            connectionState = connectionState,
            isKeepScreenAwakeEnabled = settings.isKeepScreenAwakeEnabled,
            isTheaterModeEnabled = settings.isTheaterModeEnabled,
            isHapticFeedbackEnabled = settings.isHapticFeedbackEnabled,
            shortcutApps = resolveShortcuts(supported),
            touchpadMode = touchpadMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(shortcutApps = resolveShortcuts(null)),
    )

    private var pendingDx = 0f
    private var pendingDy = 0f
    private var flushJob: Job? = null

    init {
        autoReconnect()
    }

    private fun autoReconnect() {
        viewModelScope.launch {
            val result = reconnectLastTvUseCase()
            if (result is ConnectionState.Connected) {
                _effect.send(HomeEvent.ShowMessage("Connected to ${result.tv.name}"))
            }
        }
    }

    fun onPowerClick() = send { it.sendKey(TvKey.POWER) }
    fun onRemoteButtonClick(key: TvKey) = send { it.sendKey(key) }
    fun onAppClick(channel: KnownApp) {
        send { it.launchApp(channel) }
    }

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

    fun onTouchpad(event: TouchpadEvent) {
        if (event !is TouchpadEvent.Move) {
            send { it.sendTouchpad(event) }
            return
        }
        pendingDx += event.dx
        pendingDy += event.dy
        if (flushJob?.isActive == true) return
        flushJob = viewModelScope.launch {
            delay(16)
            val dx = pendingDx;
            val dy = pendingDy
            pendingDx = 0f; pendingDy = 0f
            send { it.sendTouchpad(TouchpadEvent.Move(dx, dy)) }
        }
    }

    private fun resolveShortcuts(supported: List<KnownApp>?): List<KnownApp> {
        val pool = supported?.takeIf { it.isNotEmpty() } ?: FALLBACK_APPS
        return KnownApp.entries.filter { it in pool }.take(MAX_SHORTCUTS)
    }

    private companion object {
        const val MAX_SHORTCUTS = 5
        val FALLBACK_APPS = KnownApp.entries - KnownApp.GALAXY_PLAY
    }
}
