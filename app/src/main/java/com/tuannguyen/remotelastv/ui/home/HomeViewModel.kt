package com.tuannguyen.remotelastv.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.manager.TvControllerManager
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import com.tuannguyen.remotelastv.domain.usecase.GetTvAppsUseCase
import com.tuannguyen.remotelastv.domain.usecase.ReconnectLastTvUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val controllerManager: TvControllerManager,
    connectionManager: TvConnectionManager,
    private val reconnectLastTvUseCase: ReconnectLastTvUseCase,
    private val getTvAppsUseCase: GetTvAppsUseCase,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val supportedApps: kotlinx.coroutines.flow.Flow<List<KnownApp>?> =
        connectionManager.state
            .map { (it as? ConnectionState.Connected)?.tv }
            .distinctUntilChanged()
            .mapLatest { tv -> tv?.let { getTvAppsUseCase().getOrNull() } }

    private val _effect = Channel<HomeEvent>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.remoteSettings,
        connectionManager.state,
        supportedApps,
    ) { settings, connectionState, supported ->
        HomeUiState(
            connectionState = connectionState,
            shortcutApps = resolveShortcuts(supported),
            availableApps = resolveAvailableApps(supported),
            isKeepScreenAwakeEnabled = settings.isKeepScreenAwakeEnabled,
            isTheaterModeEnabled = settings.isTheaterModeEnabled,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            shortcutApps = resolveShortcuts(null),
            availableApps = resolveAvailableApps(null),
        ),
    )

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

    fun onAppClick(app: KnownApp) {
        send { it.launchApp(app) }
    }

    private fun send(block: suspend (TvControllerRepository) -> Result<Unit>) {
        viewModelScope.launch {
            controllerManager.execute(block).onFailure { error ->
                if (error !is TvException.NotConnected) {
                    _effect.send(HomeEvent.ShowMessage("Home action failed: ${error.message}"))
                }
            }
        }
    }

    private fun resolveAvailableApps(supported: List<KnownApp>?): List<KnownApp> {
        val pool = supported?.takeIf { it.isNotEmpty() } ?: FALLBACK_APPS
        return KnownApp.entries.filter { it in pool }
    }

    private fun resolveShortcuts(supported: List<KnownApp>?): List<KnownApp> =
        resolveAvailableApps(supported).take(MAX_SHORTCUTS - 1)

    private companion object {
        const val MAX_SHORTCUTS = 6
        val FALLBACK_APPS = KnownApp.entries - KnownApp.GALAXY_PLAY
    }
}
