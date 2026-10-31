package com.tuannguyen.remotelastv.data.tv.cast.dlna

import android.net.Uri
import android.util.Log
import com.tuannguyen.remotelastv.data.tv.cast.server.LocalMediaServer
import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.domain.model.CastPlayerState
import com.tuannguyen.remotelastv.domain.model.CastStatus
import com.tuannguyen.remotelastv.domain.repository.CastSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DlnaCastSession(
    private val device: DlnaDevice,
    private val soap: UpnpSoapClient,
    private val mediaServer: LocalMediaServer,
) : CastSession {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val castMutex = Mutex()
    private val seekMutex = Mutex()

    private val _status = MutableStateFlow(CastStatus())
    override val status: StateFlow<CastStatus> = _status.asStateFlow()

    @Volatile
    private var isLoaded = false

    @Volatile
    private var pausedSeekInProgress = false

    private var pollJob: Job? = null

    // ---------- Media ----------

    override suspend fun load(media: CastMedia): Result<Unit> =
        castMutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    suspendRunCatching {
                        stopPolling()
                        _status.value = CastStatus(state = CastPlayerState.LOADING)

                        val mimeType = media.mimeType?.takeIf { it.isNotBlank() } ?: when (media.type) {
                            CastMediaType.VIDEO -> "video/mp4"
                            CastMediaType.IMAGE -> "image/jpeg"
                            CastMediaType.AUDIO -> "audio/mpeg"
                        }
                        val published = mediaServer.publish(Uri.parse(media.contentUri), mimeType, media.type)

                        // Nhiều TV cần Stop trước khi nạp URI mới; lỗi ở bước này không quan trọng
                        suspendRunCatching { soap.stop(device) }

                        val didl = DidlLite.build(
                            media.title,
                            published.url,
                            mimeType,
                            media.type,
                            published.sizeBytes,
                        )
                        try {
                            soap.setUri(device, published.url, didl)
                        } catch (e: UpnpException) {
                            // Một số TV từ chối metadata -> thử lại không metadata
                            soap.setUri(device, published.url, "")
                        }
                        playWithRetry(device, optional = media.type == CastMediaType.IMAGE)

                        isLoaded = true
                        startPolling(device)
                    }.onFailure { e ->
                        Log.e(TAG, "DLNA cast failed for ${device.host}", e)
                        mediaServer.stop()
                        isLoaded = false
                        _status.value = CastStatus(state = CastPlayerState.ERROR, errorMessage = e.message)
                    }
                } catch (e: CancellationException) {
                    stopPolling()
                    isLoaded = false
                    mediaServer.stop()
                    _status.value = CastStatus()
                    throw e
                }
            }
        }

    /** Một số TV cần thời gian đọc metadata/media trước khi chấp nhận lệnh Play. */
    private suspend fun playWithRetry(device: DlnaDevice, optional: Boolean) {
        var last: Exception? = null
        for (attempt in 1..MAX_PLAY_ATTEMPTS) {
            try {
                soap.play(device)
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                last = e
                if (e is UpnpException && e.code !in RETRYABLE_PLAY_ERRORS) throw e
                Log.w(TAG, "DLNA Play attempt $attempt/$MAX_PLAY_ATTEMPTS failed", e)
                if (attempt < MAX_PLAY_ATTEMPTS) delay(PLAY_RETRY_DELAY_MS)
            }
        }
        if (!optional) throw last ?: IllegalStateException("Play thất bại")
    }

    // ---------- Điều khiển ----------

    override suspend fun play() = withDevice { soap.play(it) }
    override suspend fun pause() = withDevice { soap.pause(it) }

    override suspend fun stop(): Result<Unit> {
        stopPolling()
        val wasLoaded = isLoaded
        isLoaded = false
        var failure: Throwable? = null
        var cancellation: CancellationException? = null
        try {
            if (wasLoaded) soap.stop(device)
        } catch (e: CancellationException) {
            cancellation = e
        } catch (e: Exception) {
            failure = e
        }
        try {
            withContext(NonCancellable + Dispatchers.IO) { mediaServer.stop() }
        } catch (e: CancellationException) {
            val previousCancellation = cancellation
            if (previousCancellation == null) cancellation = e else previousCancellation.addSuppressed(e)
        } catch (e: Exception) {
            val previousFailure = failure
            if (previousFailure == null) failure = e else previousFailure.addSuppressed(e)
        } finally {
            _status.value = CastStatus()
            scope.cancel()
        }
        cancellation?.let { throw it }
        return failure?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    override suspend fun seekTo(positionMs: Long) = withDevice { d ->
        seekMutex.withLock {
            seekTo(d, positionMs.coerceAtLeast(0L))
        }
    }

    private suspend fun seekTo(device: DlnaDevice, target: Long) {
        try {
            soap.seek(device, target)
        } catch (e: CancellationException) {
            throw e
        } catch (seekError: Exception) {
            val wasPaused = _status.value.state == CastPlayerState.PAUSED ||
                try {
                    soap.transportInfo(device)["CurrentTransportState"] == "PAUSED_PLAYBACK"
                } catch (stateError: CancellationException) {
                    throw stateError
                } catch (stateError: Exception) {
                    seekError.addSuppressed(stateError)
                    false
                }
            if (!wasPaused) throw seekError

            pausedSeekInProgress = true
            try {
                seekWhilePaused(device, target)
                _status.update { it.copy(positionMs = target, state = CastPlayerState.PAUSED) }
            } catch (e: CancellationException) {
                throw e
            } catch (retryError: Exception) {
                retryError.addSuppressed(seekError)
                throw retryError
            } finally {
                pausedSeekInProgress = false
            }
            return
        }
        _status.update { it.copy(positionMs = target) }
    }

    private suspend fun seekWhilePaused(device: DlnaDevice, target: Long) {
        var seekFailure: Exception? = null
        var cancellation: CancellationException? = null
        try {
            soap.play(device)
            soap.seek(device, target)
        } catch (e: CancellationException) {
            cancellation = e
        } catch (e: Exception) {
            seekFailure = e
        }

        var pauseFailure: Exception? = null
        try {
            withContext(NonCancellable + Dispatchers.IO) { soap.pause(device) }
        } catch (e: CancellationException) {
            val previousCancellation = cancellation
            if (previousCancellation == null) cancellation = e else previousCancellation.addSuppressed(e)
        } catch (e: Exception) {
            pauseFailure = e
        }

        cancellation?.let { failure ->
            seekFailure?.let(failure::addSuppressed)
            pauseFailure?.let(failure::addSuppressed)
            throw failure
        }
        seekFailure?.let { failure ->
            pauseFailure?.let(failure::addSuppressed)
            throw failure
        }
        pauseFailure?.let { throw it }
    }

    override suspend fun seekBy(deltaMs: Long) = withDevice { d ->
        seekMutex.withLock {
            val current = _status.value
            val max = if (current.durationMs > 0L) current.durationMs else Long.MAX_VALUE
            val target = (current.positionMs + deltaMs).coerceIn(0L, max)
            seekTo(d, target)
        }
    }

    override suspend fun changeVolume(delta: Int) = withDevice { d ->
        val current = _status.value.volume
            ?: soap.getVolume(d)
            ?: error("TV không hỗ trợ chỉnh âm lượng qua DLNA")
        val target = (current + delta).coerceIn(0, 100)
        soap.setVolume(d, target)
        _status.update { it.copy(volume = target) }
    }

    override suspend fun setMuted(muted: Boolean) = withDevice {
        soap.setMute(it, muted)
        _status.update { status -> status.copy(isMuted = muted) }
    }

    private suspend fun withDevice(block: suspend (DlnaDevice) -> Unit): Result<Unit> =
        suspendRunCatching {
            check(isLoaded) { "Chưa có media trong phiên cast" }
            block(device)
        }

    // ---------- Poll trạng thái ----------

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun startPolling(device: DlnaDevice) {
        stopPolling()
        pollJob = scope.launch {
            var tick = 0
            var failures = 0
            var hasPlayed = false
            while (isActive) {
                try {
                    val transport = soap.transportInfo(device)
                    val position = soap.positionInfo(device)
                    var state = mapState(transport["CurrentTransportState"])
                    if (pausedSeekInProgress && state in PAUSED_SEEK_STATES) {
                        state = CastPlayerState.PAUSED
                    }
                    if (state == CastPlayerState.PLAYING) hasPlayed = true
                    // Lúc mới nạp TV có thể báo STOPPED ngắn -> coi là LOADING
                    if (state == CastPlayerState.STOPPED && !hasPlayed && tick < 8) state = CastPlayerState.LOADING
                    val volume = if (tick % 3 == 0) suspendRunCatching { soap.getVolume(device) }.getOrNull() else null
                    failures = 0
                    _status.update { old ->
                        old.copy(
                            state = state,
                            positionMs = UpnpTime.parse(position["RelTime"]) ?: old.positionMs,
                            durationMs = UpnpTime.parse(position["TrackDuration"]) ?: old.durationMs,
                            volume = volume ?: old.volume,
                            errorMessage = null,
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (++failures >= 5) {
                        _status.update { it.copy(state = CastPlayerState.ERROR, errorMessage = "Mất kết nối với TV") }
                        break
                    }
                }
                tick++
                delay(1_000)
            }
        }
    }

    private fun mapState(raw: String?): CastPlayerState = when (raw) {
        "PLAYING" -> CastPlayerState.PLAYING
        "PAUSED_PLAYBACK", "PAUSED_RECORDING" -> CastPlayerState.PAUSED
        "TRANSITIONING" -> CastPlayerState.LOADING
        "STOPPED", "NO_MEDIA_PRESENT" -> CastPlayerState.STOPPED
        else -> CastPlayerState.LOADING
    }

    private companion object {
        const val TAG = "DlnaCastSession"
        const val MAX_PLAY_ATTEMPTS = 15
        const val PLAY_RETRY_DELAY_MS = 1_000L
        val RETRYABLE_PLAY_ERRORS = setOf(701, 705)
        val PAUSED_SEEK_STATES = setOf(
            CastPlayerState.PAUSED,
            CastPlayerState.PLAYING,
            CastPlayerState.LOADING,
        )
    }
}