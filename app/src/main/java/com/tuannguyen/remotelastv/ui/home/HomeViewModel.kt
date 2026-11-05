package com.tuannguyen.remotelastv.ui.home

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.manager.TvControllerManager
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.InterstitialAdPolicy
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.repository.CastAdCounterRepository
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    settingsRepository: SettingsRepository,
    private val castAdCounterRepository: CastAdCounterRepository,
    private val controllerManager: TvControllerManager,
    connectionManager: TvConnectionManager,
    private val reconnectLastTvUseCase: ReconnectLastTvUseCase,
    private val getTvAppsUseCase: GetTvAppsUseCase,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val supportedApps: Flow<List<KnownApp>?> =
        connectionManager.state
            .map { (it as? ConnectionState.Connected)?.tv }
            .distinctUntilChanged()
            .mapLatest { tv -> tv?.let { getTvAppsUseCase().getOrNull() } }

    private val _effect = Channel<HomeEvent>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var isHomeForeground = false
    private var isRemotePageVisible = false
    private var isRemotePageReady = false
    private var homeAdCycleJob: Job? = null
    private var homeAdCycleStartedAt = 0L
    private var hasRemoteInteractionInCycle = false
    private var lastRemoteInteractionAt = 0L
    private var awaitingInterstitialResult = false

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

    fun onHomeResumed() {
        isHomeForeground = true
        startHomeAdCycle()
    }

    fun onHomePaused() {
        isHomeForeground = false
        stopHomeAdCycle()
    }

    fun onRemotePageVisibilityChanged(visible: Boolean) {
        isRemotePageVisible = visible
    }

    fun onRemotePageReadyChanged(ready: Boolean) {
        isRemotePageReady = ready
    }

    fun onRemoteInteraction() {
        if (!canShowHomeInterstitial() || homeAdCycleStartedAt == 0L) return
        hasRemoteInteractionInCycle = true
        lastRemoteInteractionAt = SystemClock.elapsedRealtime()
    }

    fun onInterstitialFinished() {
        if (!awaitingInterstitialResult) return
        awaitingInterstitialResult = false
        startHomeAdCycle()
    }

    fun onInterstitialUnavailable() {
        if (!awaitingInterstitialResult) return
        awaitingInterstitialResult = false
        startHomeAdCycle()
    }

    fun canShowHomeInterstitialNow(): Boolean =
        awaitingInterstitialResult &&
                canShowHomeInterstitial() &&
                hasRemoteInteractionInCycle &&
                SystemClock.elapsedRealtime() - lastRemoteInteractionAt >=
                InterstitialAdPolicy.HOME_IDLE_MILLIS

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

    private fun canShowHomeInterstitial(): Boolean =
        isHomeForeground &&
                isRemotePageVisible &&
                isRemotePageReady

    private fun stopHomeAdCycle() {
        homeAdCycleJob?.cancel()
        homeAdCycleJob = null
        awaitingInterstitialResult = false
        homeAdCycleStartedAt = 0L
        hasRemoteInteractionInCycle = false
        lastRemoteInteractionAt = 0L
    }

    private fun startHomeAdCycle() {
        homeAdCycleJob?.cancel()
        homeAdCycleStartedAt = SystemClock.elapsedRealtime()
        hasRemoteInteractionInCycle = false
        lastRemoteInteractionAt = 0L
        awaitingInterstitialResult = false
        if (!isHomeForeground) return

        val cycleStartedAt = homeAdCycleStartedAt
        homeAdCycleJob = viewModelScope.launch {
            delay(
                max(0L, cycleStartedAt + InterstitialAdPolicy.HOME_CYCLE_MILLIS -
                        SystemClock.elapsedRealtime()).milliseconds
            )
            if (!isCurrentHomeAdCycle(cycleStartedAt)) return@launch

            if (!hasRemoteInteractionInCycle) {
                startHomeAdCycle()
                return@launch
            }

            while (isCurrentHomeAdCycle(cycleStartedAt)) {
                val idleFor = SystemClock.elapsedRealtime() - lastRemoteInteractionAt
                val remainingIdleTime = InterstitialAdPolicy.HOME_IDLE_MILLIS - idleFor
                if (remainingIdleTime > 0L) {
                    delay(remainingIdleTime.milliseconds)
                    continue
                }

                val cooldownElapsed = try {
                    castAdCounterRepository.isInterstitialCooldownElapsed(
                        InterstitialAdPolicy.COOLDOWN_MILLIS,
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Log.e(TAG, "Failed to check interstitial cooldown", error)
                    false
                }

                if (!isCurrentHomeAdCycle(cycleStartedAt)) return@launch
                if (
                    SystemClock.elapsedRealtime() - lastRemoteInteractionAt <
                    InterstitialAdPolicy.HOME_IDLE_MILLIS
                ) {
                    continue
                }
                if (cooldownElapsed && canShowHomeInterstitial()) {
                    awaitingInterstitialResult = true
                    _effect.send(HomeEvent.ShowInterstitial)
                } else {
                    startHomeAdCycle()
                }
                return@launch
            }
        }
    }

    private fun isCurrentHomeAdCycle(startedAt: Long): Boolean =
        homeAdCycleStartedAt == startedAt && isHomeForeground

    fun onPowerClick() {
        onRemoteInteraction()
        send { it.sendKey(TvKey.POWER) }
    }

    fun onAppClick(app: KnownApp) {
        onRemoteInteraction()
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
        const val TAG = "HomeViewModel"
        const val MAX_SHORTCUTS = 6
        val FALLBACK_APPS = KnownApp.entries - KnownApp.GALAXY_PLAY
    }
}
