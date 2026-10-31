package com.tuannguyen.remotelastv.ui.cast_video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tuannguyen.remotelastv.di.ApplicationScope
import com.tuannguyen.remotelastv.domain.model.CastException
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.domain.model.CastPlayerState
import com.tuannguyen.remotelastv.domain.usecase.CastPlaybackUseCase
import com.tuannguyen.remotelastv.domain.usecase.GetConnectedTelevisionUseCase
import com.tuannguyen.remotelastv.domain.usecase.GetMediaInfoUseCase
import com.tuannguyen.remotelastv.domain.usecase.GetVideoThumbnailUseCase
import com.tuannguyen.remotelastv.domain.usecase.StartCastUseCase
import com.tuannguyen.remotelastv.ui.navigation.Route
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

@HiltViewModel
class CastVideoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getConnectedTelevision: GetConnectedTelevisionUseCase,
    private val getMediaInfo: GetMediaInfoUseCase,
    private val getVideoThumbnail: GetVideoThumbnailUseCase,
    private val startCast: StartCastUseCase,
    private val playback: CastPlaybackUseCase,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {

    private val mediaUri = savedStateHandle.toRoute<Route.CastVideo>().uri

    private val _uiState = MutableStateFlow(CastVideoUiState())
    val uiState: StateFlow<CastVideoUiState> = _uiState.asStateFlow()

    private val _events = Channel<CastVideoEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var stopped = false
    private var statusJob: Job? = null

    init {
        startCasting()
    }

    private fun startCasting() = viewModelScope.launch {
        try {
            val television = getConnectedTelevision()
                ?: return@launch failAndExit(CastFailReason.DEVICE_NOT_FOUND)
            val mediaInfo = getMediaInfo(mediaUri)
            _uiState.update { it.copy(title = mediaInfo.title, tvName = television.name) }

            val media = CastMedia(
                contentUri = mediaUri,
                type = CastMediaType.VIDEO,
                title = mediaInfo.title,
                mimeType = mediaInfo.mimeType,
            )
            startCast(television, media)
                .onSuccess {
                    observe()
                    val thumbnailBytes = getVideoThumbnail(mediaUri)
                    _uiState.update { it.copy(thumbnailBytes = thumbnailBytes) }
                }
                .onFailure { error ->
                    android.util.Log.e(TAG, "Failed to cast media to ${television.ipAddress}", error)
                    failAndExit(failReason(error))
                }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to prepare media for casting", e)
            failAndExit(failReason(e))
        }
    }

    private fun observe() {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            playback.status.collect { status ->
                _uiState.update {
                    it.copy(
                        isLoading = status.state == CastPlayerState.LOADING,
                        isPlaying = status.state == CastPlayerState.PLAYING,
                        positionMs = status.positionMs,
                        durationMs = status.durationMs,
                        volume = status.volume,
                        isMuted = status.isMuted,
                    )
                }
                if (status.state == CastPlayerState.ERROR) {
                    android.util.Log.e(TAG, "Cast playback failed: ${status.errorMessage}")
                    failAndExit(CastFailReason.UNKNOWN)
                }
            }
        }
    }

    fun onPlayPauseClick() = withCast {
        if (_uiState.value.isPlaying) playback.pause() else playback.play()
    }

    fun onRewindClick() = withCast { playback.seekBy(-SEEK_STEP_MS) }

    fun onForwardClick() = withCast { playback.seekBy(SEEK_STEP_MS) }

    fun onVolumeUpClick() = withCast { playback.changeVolume(VOLUME_STEP) }

    fun onVolumeDownClick() = withCast { playback.changeVolume(-VOLUME_STEP) }

    fun onMuteClick() = withCast { playback.setMuted(!_uiState.value.isMuted) }

    fun onSeekTo(positionMs: Long) = withCast { playback.seekTo(positionMs) }

    fun onRetryCasting() {
        stopped = false
        statusJob?.cancel()
        _uiState.update { it.copy(isLoading = true, thumbnailBytes = null) }
        startCasting()
    }

    fun onStopCastingClick() {
        stopOnce { _events.send(CastVideoEvent.NavigateBack) }
    }

    private fun withCast(command: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            command().onFailure { error ->
                _events.send(CastVideoEvent.ShowMessage(failReason(error)))
            }
        }
    }

    private fun failReason(error: Throwable): CastFailReason =
        (error as? CastException)?.reason ?: CastFailReason.UNKNOWN

    private fun failAndExit(reason: CastFailReason) {
        _uiState.update { it.copy(isLoading = false) }
        stopOnce { _events.send(CastVideoEvent.CastFailed(reason)) }
    }

    private fun stopOnce(afterStop: (suspend () -> Unit)? = null) {
        if (stopped) return
        stopped = true
        appScope.launch {
            playback.stop().onFailure { error ->
                android.util.Log.e(TAG, "Failed to stop casting", error)
            }
            afterStop?.invoke()
        }
    }

    override fun onCleared() {
        stopOnce()
        super.onCleared()
    }

    private companion object {
        const val TAG = "CastVideoViewModel"
        const val SEEK_STEP_MS = 10_000L
        const val VOLUME_STEP = 5
    }
}
