package com.tuannguyen.remotelastv.domain.connection

import android.util.Log
import com.tuannguyen.remotelastv.di.ApplicationScope
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.ConnectionStatus
import com.tuannguyen.remotelastv.domain.model.FailReason
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TvCredentials
import com.tuannguyen.remotelastv.domain.repository.PinPairable
import com.tuannguyen.remotelastv.domain.repository.SavedTvRepository
import com.tuannguyen.remotelastv.domain.repository.TvConnectionRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.ConnectException
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class TvConnectionManager @Inject constructor(
    private val repositories: Map<TelevisionBrand, @JvmSuppressWildcards TvConnectionRepository>,
    private val savedTvRepository: SavedTvRepository,
    private val lifecycleTracker: AppLifecycleTracker,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _errors = Channel<FailReason>(Channel.BUFFERED)
    val errors: Flow<FailReason> = _errors.receiveAsFlow()

    val connectedTelevision: StateFlow<Television?> = state
        .map { (it as? ConnectionState.Connected)?.tv }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private var pending: CompletableDeferred<Result<Television>>? = null

    private val lock = Mutex()
    private var job: Job? = null
    private var activeRepo: TvConnectionRepository? = null

    private var currentTargetTv: Television? = null
    private var userDisconnected: Boolean = false

    init {
        scope.launch {
            lifecycleTracker.isForeground.collect { isFg ->
                if (isFg) {
                    onAppForegrounded()
                }
            }
        }

        scope.launch {
            state
                .map { (it as? ConnectionState.Connected)?.tv?.id }
                .distinctUntilChanged()
                .flatMapLatest { id ->
                    if (id == null) emptyFlow() else savedTvRepository.observe(id)
                }
                .filterNotNull()
                .collect { saved ->
                    _state.update { cur ->
                        if (cur is ConnectionState.Connected &&
                            cur.tv.id == saved.id &&
                            cur.tv.name != saved.name
                        ) cur.copy(tv = cur.tv.copy(name = saved.name))
                        else cur
                    }
                    currentTargetTv?.let { target ->
                        if (target.id == saved.id) currentTargetTv = target.copy(name = saved.name)
                    }
                }
        }
    }

    private suspend fun onAppForegrounded() {
        if (userDisconnected) return

        lock.withLock {
            val targetTv = currentTargetTv ?: return
            if (_state.value !is ConnectionState.TempDisconnected) return

            Log.d("TvConnection", "App foregrounded: resuming ${targetTv.name}")
            startConnection(targetTv, showConnectionState = true)
        }
    }

    suspend fun connect(
        tv: Television,
        showConnectionState: Boolean = true,
    ): Result<Television> {
        val deferredToAwait = lock.withLock {
            userDisconnected = false
            currentTargetTv = tv

            val current = _state.value
            if (current is ConnectionState.Connected && current.tv.id == tv.id) {
                return Result.success(current.tv)
            }

            val inFlightTv = when (current) {
                is ConnectionState.Connecting -> current.tv
                is ConnectionState.Reconnecting -> current.tv
                else -> null
            }
            val existingPending = pending

            if (existingPending != null && inFlightTv != null &&
                inFlightTv.id == tv.id && inFlightTv.ipAddress == tv.ipAddress
            ) {
                existingPending
            } else {
                startConnection(tv, showConnectionState)
                    ?: return Result.failure(IllegalStateException("Unsupported brand"))
            }
        }
        return deferredToAwait.await()
    }

    fun disconnect() {
        scope.launch {
            lock.withLock {
                userDisconnected = true
                currentTargetTv = null
                stopCurrent()
                _state.value = ConnectionState.Disconnected
            }
        }
    }

    suspend fun submitPin(pin: String) {
        (activeRepo as? PinPairable)?.submitPin(pin)
    }

    suspend fun cancelSendPin() {
        (activeRepo as? PinPairable)?.cancelSendPin()
    }

    private suspend fun stopCurrent() {
        pending?.cancel()
        pending = null
        job?.cancelAndJoin()
        job = null
        activeRepo?.disconnect()
        activeRepo = null
    }

    private suspend fun runConnection(
        repo: TvConnectionRepository,
        tv: Television,
        showConnectionState: Boolean,
    ) {
        val credentials = savedTvRepository.getCredentials(tv.id)
        var attempt = 0
        while (true) {
            var last: ConnectionStatus? = null
            repo.connect(tv, credentials).collect { status ->
                last = status
                when (status) {
                    ConnectionStatus.Connecting -> if (showConnectionState) {
                        _state.value = if (attempt == 0) ConnectionState.Connecting(tv)
                        else ConnectionState.Reconnecting(tv, attempt)
                    }

                    ConnectionStatus.WaitingForPin -> if (showConnectionState) {
                        _state.value = ConnectionState.WaitingForPin(tv)
                    }

                    ConnectionStatus.WaitingForAuth -> if (showConnectionState) {
                        _state.value = ConnectionState.Connecting(tv)
                    }

                    is ConnectionStatus.Connected -> {
                        attempt = 0
                        val savedTv = saveConnected(tv, status.credentials)
                        currentTargetTv = savedTv
                        _state.value = ConnectionState.Connected(savedTv)
                        pending?.complete(Result.success(savedTv))
                        pending = null
                    }

                    is ConnectionStatus.Failed -> if (showConnectionState) {
                        _state.value = ConnectionState.Failed(tv, status.reason)
                    }

                    else -> Unit
                }
            }

            when (val end = last) {
                is ConnectionStatus.Disconnected -> {
                    if (end.recoverable && attempt < BACKOFF_MS.size) {
                        attempt++
                        if (showConnectionState) {
                            _state.value = ConnectionState.Reconnecting(tv, attempt)
                        }
                        delay(BACKOFF_MS[attempt - 1])
                        continue
                    }
                    _state.value = ConnectionState.Disconnected
                    pending?.complete(Result.failure(ConnectException("Disconnected")))
                    pending = null
                    return
                }

                is ConnectionStatus.Failed -> {
                    _state.value = if (showConnectionState) {
                        ConnectionState.Failed(tv, end.reason)
                    } else {
                        ConnectionState.Disconnected
                    }
                    if (showConnectionState) _errors.trySend(end.reason)
                    pending?.complete(Result.failure(ConnectException(end.reason.name)))
                    pending = null
                    return
                }

                is ConnectionStatus.TempDisconnected -> {
                    _state.value = ConnectionState.TempDisconnected(tv)

                    if (!lifecycleTracker.isForeground.value) {
                        pending?.complete(Result.failure(ConnectException("Temporarily disconnected")))
                        pending = null
                        if (!lifecycleTracker.isForeground.value) return
                        attempt = 0
                        continue
                    }

                    if (attempt < BACKOFF_MS.size) {
                        attempt++
                        delay(BACKOFF_MS[attempt - 1])
                        continue
                    }
                    pending?.complete(Result.failure(ConnectException("Disconnected")))
                    pending = null
                    _state.value = ConnectionState.Disconnected
                    return
                }

                else -> {
                    _state.value = ConnectionState.Disconnected
                    pending?.complete(Result.failure(ConnectException("Connection ended")))
                    pending = null
                    return
                }
            }
        }
    }

    fun acknowledgeFailure() {
        _state.update { state ->
            if (state is ConnectionState.Failed) {
                ConnectionState.Disconnected
            } else {
                state
            }
        }
    }

    private suspend fun startConnection(
        tv: Television,
        showConnectionState: Boolean,
    ): CompletableDeferred<Result<Television>>? {
        stopCurrent()
        if (!showConnectionState) {
            _state.value = ConnectionState.Disconnected
        }
        val repo = repositories[tv.brand]
        if (repo == null) {
            if (showConnectionState) _errors.trySend(FailReason.UNSUPPORTED_BRAND)
            return null
        }
        val deferred = CompletableDeferred<Result<Television>>()
        pending = deferred
        activeRepo = repo
        job = scope.launch { runConnection(repo, tv, showConnectionState) }
        return deferred
    }

    private suspend fun saveConnected(tv: Television, credentials: TvCredentials): Television {
        try {
            savedTvRepository.save(tv, credentials)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("TvConnection", "save failed", e)
        }
        return savedTvRepository.get(tv.id) ?: tv
    }

    private companion object {
        val BACKOFF_MS = longArrayOf(1_000, 2_000, 4_000)
    }
}