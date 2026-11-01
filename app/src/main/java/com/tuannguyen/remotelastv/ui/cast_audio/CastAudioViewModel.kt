package com.tuannguyen.remotelastv.ui.cast_audio

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.data.media.AudioLibraryReader
import com.tuannguyen.remotelastv.di.ApplicationScope
import com.tuannguyen.remotelastv.domain.model.AudioTrack
import com.tuannguyen.remotelastv.domain.model.CastException
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.domain.model.CastPlayerState
import com.tuannguyen.remotelastv.domain.model.CastRepeatMode
import com.tuannguyen.remotelastv.domain.usecase.CastPlaybackUseCase
import com.tuannguyen.remotelastv.domain.usecase.GetConnectedTelevisionUseCase
import com.tuannguyen.remotelastv.domain.usecase.StartCastUseCase
import com.tuannguyen.remotelastv.domain.usecase.StopCastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class CastAudioViewModel @Inject constructor(
    private val getConnectedTelevision: GetConnectedTelevisionUseCase,
    private val startCast: StartCastUseCase,
    private val playback: CastPlaybackUseCase,
    private val stopCast: StopCastUseCase,
    private val audioLibrary: AudioLibraryReader,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CastAudioUiState())
    val uiState: StateFlow<CastAudioUiState> = _uiState.asStateFlow()
    private val _events = Channel<CastAudioEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val commandMutex = Mutex()
    private val trackMutex = Mutex()
    private var libraryLoadJob: Job? = null
    private var statusJob: Job? = null
    private var observedPlayingUri: String? = null

    init {
        observe()
        loadAudioLibrary()
    }

    private fun loadAudioLibrary() {
        libraryLoadJob?.cancel()
        libraryLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLibraryLoading = true) }
            try {
                val tracks = loadTracks()
                _uiState.update { state ->
                    val uris = tracks.map(AudioTrack::uri)
                    state.copy(
                        tracks = tracks,
                        playbackOrder = if (state.isShuffleEnabled) {
                            uris.shuffled()
                        } else {
                            uris
                        },
                        isLibraryLoading = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load audio library", e)
                _uiState.update { it.copy(isLibraryLoading = false) }
                fail(failReason(e))
            }
        }
    }

    private suspend fun loadTracks(): List<AudioTrack> = audioLibrary.readAll()

    private fun observe() {
        statusJob = viewModelScope.launch {
            playback.status.collect { status ->
                val currentState = _uiState.value
                val currentUri = currentState.currentTrackUri
                if (currentUri == null || status.mediaUri != currentUri) return@collect
                val trackDurationMs = currentState.tracks
                    .firstOrNull { it.uri == currentUri }
                    ?.durationMs
                    ?: 0L
                val durationMs = status.durationMs.takeIf { it > 0L } ?: trackDurationMs

                if (status.state == CastPlayerState.PLAYING) {
                    observedPlayingUri = currentUri
                }
                _uiState.update {
                    it.copy(
                        isLoading = status.state == CastPlayerState.LOADING,
                        isPlaying = status.state == CastPlayerState.PLAYING,
                        positionMs = status.positionMs,
                        durationMs = durationMs,
                        volume = status.volume,
                        isMuted = status.isMuted,
                    )
                }
                if (status.state == CastPlayerState.ERROR) {
                    Log.e(TAG, "Audio cast failed: ${status.errorMessage}")
                    fail(CastFailReason.UNKNOWN)
                }
                if (
                    status.state == CastPlayerState.STOPPED &&
                    observedPlayingUri == currentUri &&
                    isAtTrackEnd(status.positionMs, durationMs)
                ) {
                    observedPlayingUri = null
                    advanceAfterCompletion(currentUri)
                }
            }
        }
    }

    fun onPlayPauseClick() = withCast {
        if (_uiState.value.isPlaying) playback.pause() else playback.play()
    }

    fun onPreviousClick() = viewModelScope.launch {
        trackMutex.withLock {
            val state = _uiState.value
            val currentUri = state.currentTrackUri ?: return@withLock
            if (state.positionMs > PREVIOUS_RESTART_THRESHOLD_MS) {
                playback.seekTo(0L).onFailure { error ->
                    _events.send(CastAudioEvent.ShowMessage(failReason(error)))
                }
                return@withLock
            }
            val previousUri = state.playbackOrder.previousOf(currentUri) ?: return@withLock
            state.tracks.firstOrNull { it.uri == previousUri }?.let { castTrack(it) }
        }
    }

    fun onNextClick() = viewModelScope.launch {
        trackMutex.withLock {
            val state = _uiState.value
            val currentUri = state.currentTrackUri ?: return@withLock
            val nextUri = state.playbackOrder.nextOf(currentUri) ?: return@withLock
            state.tracks.firstOrNull { it.uri == nextUri }?.let { castTrack(it) }
        }
    }

    fun onRepeatClick() {
        _uiState.update {
            it.copy(
                repeatMode = when (it.repeatMode) {
                    CastRepeatMode.OFF -> CastRepeatMode.ALL
                    CastRepeatMode.ALL -> CastRepeatMode.ONE
                    CastRepeatMode.ONE -> CastRepeatMode.OFF
                },
            )
        }
    }

    fun onShuffleClick() {
        _uiState.update { state ->
            val orderedUris = state.tracks.map(AudioTrack::uri)
            val currentUri = state.currentTrackUri
            if (state.isShuffleEnabled) {
                state.copy(
                    isShuffleEnabled = false,
                    playbackOrder = orderedUris,
                )
            } else if (currentUri == null) {
                state.copy(
                    isShuffleEnabled = true,
                    playbackOrder = orderedUris.shuffled(),
                )
            } else {
                val currentIndex = orderedUris.indexOf(currentUri).coerceAtLeast(0)
                val played = orderedUris.take(currentIndex + 1)
                val remaining = orderedUris.drop(currentIndex + 1).shuffled()
                state.copy(
                    isShuffleEnabled = true,
                    playbackOrder = played + remaining,
                )
            }
        }
    }

    fun onTrackClick(trackUri: String) {
        _uiState.update { it.copy(isMusicPickerVisible = false) }
        viewModelScope.launch {
            trackMutex.withLock {
                _uiState.value.tracks.firstOrNull { it.uri == trackUri }?.let { castTrack(it) }
            }
        }
    }

    fun onMusicPickerRequested() {
        _uiState.update { it.copy(isMusicPickerVisible = true) }
    }

    fun onMusicPickerDismissed() {
        _uiState.update { it.copy(isMusicPickerVisible = false) }
    }

    fun onVolumeUpClick() = withCast(dropIfBusy = true) { playback.changeVolume(VOLUME_STEP) }
    fun onVolumeDownClick() = withCast(dropIfBusy = true) { playback.changeVolume(-VOLUME_STEP) }
    fun onMuteClick() = withCast { playback.setMuted(!_uiState.value.isMuted) }
    fun onSeekTo(positionMs: Long) = withCast { playback.seekTo(positionMs) }

    fun onRetryCasting() {
        val selectedUri = _uiState.value.currentTrackUri
        if (selectedUri == null) {
            loadAudioLibrary()
        } else {
            viewModelScope.launch {
                trackMutex.withLock {
                    _uiState.value.tracks
                        .firstOrNull { it.uri == selectedUri }
                        ?.let { castTrack(it) }
                }
            }
        }
    }

    fun onStopCastingClick() {
        libraryLoadJob?.cancel()
        statusJob?.cancel()
        _events.trySend(CastAudioEvent.NavigateBack)
    }

    private fun withCast(
        dropIfBusy: Boolean = false,
        command: suspend () -> Result<Unit>,
    ) {
        viewModelScope.launch {
            val acquired = if (dropIfBusy) {
                commandMutex.tryLock()
            } else {
                commandMutex.lock()
                true
            }
            if (!acquired) return@launch
            try {
                command().onFailure { error ->
                    _events.send(CastAudioEvent.ShowMessage(failReason(error)))
                }
            } finally {
                commandMutex.unlock()
            }
        }
    }

    private fun failReason(error: Throwable): CastFailReason =
        (error as? CastException)?.reason ?: CastFailReason.UNKNOWN

    private fun fail(reason: CastFailReason) {
        _uiState.update { it.copy(isLoading = false) }
        _events.trySend(CastAudioEvent.CastFailed(reason))
    }

    private suspend fun castTrack(
        track: AudioTrack,
        television: com.tuannguyen.remotelastv.domain.model.Television? = null,
    ) {
        try {
            val targetTelevision = television ?: getConnectedTelevision()
                ?: return fail(CastFailReason.DEVICE_NOT_FOUND)
            observedPlayingUri = null
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isPlaying = false,
                    currentTrackUri = track.uri,
                    title = track.title,
                    durationMs = track.durationMs,
                    positionMs = 0L,
                    tvName = targetTelevision.name,
                )
            }
            val media = CastMedia(
                contentUri = track.uri,
                type = CastMediaType.AUDIO,
                title = track.title,
                mimeType = track.mimeType,
            )
            startCast(targetTelevision, media).onFailure { error ->
                Log.e(TAG, "Failed to cast audio to ${targetTelevision.ipAddress}", error)
                fail(failReason(error))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare track for casting", e)
            fail(failReason(e))
        }
    }

    private fun advanceAfterCompletion(completedUri: String) {
        viewModelScope.launch {
            trackMutex.withLock {
                val state = _uiState.value
                if (state.currentTrackUri != completedUri) return@withLock
                val currentIndex = state.playbackOrder.indexOf(completedUri)
                if (currentIndex < 0 || state.playbackOrder.isEmpty()) return@withLock
                val nextUri = when {
                    state.repeatMode == CastRepeatMode.ONE -> completedUri
                    currentIndex < state.playbackOrder.lastIndex ->
                        state.playbackOrder[currentIndex + 1]
                    state.repeatMode == CastRepeatMode.ALL ->
                        state.playbackOrder.first()
                    else -> return@withLock
                }
                state.tracks.firstOrNull { it.uri == nextUri }?.let { castTrack(it) }
            }
        }
    }

    private fun isAtTrackEnd(positionMs: Long, durationMs: Long): Boolean =
        durationMs > 0L && positionMs >= (durationMs - END_POSITION_TOLERANCE_MS).coerceAtLeast(0L)

    private fun List<String>.nextOf(uri: String): String? {
        val index = indexOf(uri)
        if (isEmpty() || index < 0) return null
        return this[(index + 1) % size]
    }

    private fun List<String>.previousOf(uri: String): String? {
        val index = indexOf(uri)
        if (isEmpty() || index < 0) return null
        return this[(index - 1 + size) % size]
    }

    override fun onCleared() {
        if (_uiState.value.currentTrackUri != null) {
            appScope.launch {
                playback.stop().onFailure { error ->
                    Log.e(TAG, "Failed to stop audio casting", error)
                }
                stopCast()
            }
        }
        super.onCleared()
    }

    private companion object {
        const val TAG = "CastAudioViewModel"
        const val VOLUME_STEP = 5
        const val PREVIOUS_RESTART_THRESHOLD_MS = 3_000L
        const val END_POSITION_TOLERANCE_MS = 2_500L
    }
}
