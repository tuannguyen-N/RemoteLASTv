package com.tuannguyen.remotelastv.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.manager.TvControllerManager
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.TouchpadEvent
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.model.VolumeState
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import com.tuannguyen.remotelastv.domain.usecase.GetTvAppsUseCase
import com.tuannguyen.remotelastv.domain.usecase.ReconnectLastTvUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val controllerManager: TvControllerManager,
    connectionManager: TvConnectionManager,
    private val reconnectLastTvUseCase: ReconnectLastTvUseCase,
    private val getTvAppsUseCase: GetTvAppsUseCase
) : ViewModel() {
    private data class TvLiveState(
        val touchpadMode: TouchpadMode,
        val volume: VolumeState?,
        val capabilities: Set<TvCapability>,
    )

    private val tvLiveState: Flow<TvLiveState> = combine(
        controllerManager.touchpadMode,
        controllerManager.volume,
        controllerManager.capabilities,
    ) { touchpadMode, volume, capabilities ->
        TvLiveState(touchpadMode, volume, capabilities)
    }

    private val voiceLiveState: Flow<Pair<TvLiveState, Boolean>> =
        combine(tvLiveState, controllerManager.voiceRecording) { tvLive, isRecording ->
            tvLive to isRecording
        }

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
        voiceLiveState,
    ) { local, settings, connectionState, supported, live ->
        val (tvLive, isRecording) = live
        local.copy(
            showMediaButtons = settings.showMediaButtons,
            navigationMode = settings.navigationMode,
            connectionState = connectionState,
            isAutoReconnectEnabled = settings.isAutoReconnectEnabled,
            isKeepScreenAwakeEnabled = settings.isKeepScreenAwakeEnabled,
            isTheaterModeEnabled = settings.isTheaterModeEnabled,
            isHapticFeedbackEnabled = settings.isHapticFeedbackEnabled,
            isSmoothPressEnabled = settings.isSmoothPressEnabled,
            isAutoShowKeyboardEnabled = settings.isAutoShowKeyboardEnabled,
            hasRequestedMicrophonePermission = settings.hasRequestedMicrophonePermission,
            showFirstConnectTutorial =
                connectionState is ConnectionState.Connected && !settings.hasSeenRemoteTutorial,
            shortcutApps = resolveShortcuts(supported),
            availableApps = resolveAvailableApps(supported),
            touchpadMode = tvLive.touchpadMode,
            volume = tvLive.volume,
            capabilities = tvLive.capabilities,
            isRecording = isRecording,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            shortcutApps = resolveShortcuts(null),
            availableApps = resolveAvailableApps(null),
        ),
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
                _effect.send(
                    HomeEvent.ShowMessage(
                        context.getString(R.string.home_connected_to_tv, result.tv.name)
                    )
                )
            }
        }
    }

    fun onPowerClick() = send { it.sendKey(TvKey.POWER) }
    fun onRemoteButtonClick(key: TvKey) = send { it.sendKey(key) }
    fun onAppClick(channel: KnownApp) {
        send { it.launchApp(channel) }
    }

    fun startVoiceRecording() {
        viewModelScope.launch {
            controllerManager.execute { it.startVoiceRecording() }
                .onFailure { e ->
                    _effect.send(
                        HomeEvent.ShowMessage(
                            context.getString(
                                R.string.voice_recording_start_failed,
                                e.message ?: context.getString(R.string.error_unknown),
                            )
                        )
                    )
                }
        }
    }

    fun stopVoiceRecording() {
        viewModelScope.launch {
            controllerManager.execute { it.stopVoiceRecording() }
                .onFailure { e ->
                    _effect.send(
                        HomeEvent.ShowMessage(
                            context.getString(
                                R.string.voice_recording_stop_failed,
                                e.message ?: context.getString(R.string.error_unknown),
                            )
                        )
                    )
                }
        }
    }

    fun onMicrophonePermissionDenied() {
        viewModelScope.launch {
            _effect.send(
                HomeEvent.ShowMessage(context.getString(R.string.microphone_permission_denied))
            )
        }
    }

    fun onMicrophonePermissionRequested() {
        viewModelScope.launch {
            settingsRepository.setHasRequestedMicrophonePermission(true)
        }
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
            delay(10)
            val dx = pendingDx;
            val dy = pendingDy
            pendingDx = 0f; pendingDy = 0f
            send { it.sendTouchpad(TouchpadEvent.Move(dx, dy)) }
        }
    }

    fun onTutorialFinished() {
        viewModelScope.launch {
            settingsRepository.setHasSeenRemoteTutorial(true)
        }
    }

    fun onTutorialNavigationModeChanged(mode: NavigationMode) {
        updateSettings { setNavigationMode(mode) }
    }

    fun onTutorialMediaControlsChanged(enabled: Boolean) {
        updateSettings { setShowMediaButtons(enabled) }
    }

    fun onTutorialAutoReconnectChanged(enabled: Boolean) {
        updateSettings { setAutoReconnectEnabled(enabled) }
    }

    fun onTutorialKeepScreenAwakeChanged(enabled: Boolean) {
        updateSettings { setKeepScreenAwakeEnabled(enabled) }
    }

    fun onTutorialTheaterModeChanged(enabled: Boolean) {
        updateSettings { setTheaterModeEnabled(enabled) }
    }

    fun onTutorialHapticFeedbackChanged(enabled: Boolean) {
        updateSettings { setHapticFeedbackEnabled(enabled) }
    }

    fun onTutorialSmoothPressChanged(enabled: Boolean) {
        updateSettings { setSmoothPressEnabled(enabled) }
    }

    fun onTutorialAutoShowKeyboardChanged(enabled: Boolean) {
        updateSettings { setAutoShowKeyboardEnabled(enabled) }
    }

    private fun updateSettings(update: suspend SettingsRepository.() -> Unit) {
        viewModelScope.launch {
            settingsRepository.update()
        }
    }

    private fun resolveAvailableApps(
        supported: List<KnownApp>?,
    ): List<KnownApp> {
        val pool = supported?.takeIf { it.isNotEmpty() } ?: FALLBACK_APPS
        return KnownApp.entries.filter { it in pool }
    }

    private fun resolveShortcuts(
        supported: List<KnownApp>?,
    ): List<KnownApp> {
        return resolveAvailableApps(supported).take(MAX_SHORTCUTS - 1)
    }

    private companion object {
        const val MAX_SHORTCUTS = 6
        val FALLBACK_APPS = KnownApp.entries - KnownApp.GALAXY_PLAY
    }
}
