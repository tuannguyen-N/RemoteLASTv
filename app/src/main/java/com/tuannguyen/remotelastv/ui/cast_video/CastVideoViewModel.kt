package com.tuannguyen.remotelastv.ui.cast_video

import android.net.Uri
import android.util.Log
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
import com.tuannguyen.remotelastv.domain.usecase.StopCastUseCase
import com.tuannguyen.remotelastv.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

@HiltViewModel
class CastVideoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getConnectedTelevision: GetConnectedTelevisionUseCase,
    private val getMediaInfo: GetMediaInfoUseCase,
    private val getVideoThumbnail: GetVideoThumbnailUseCase,
    private val startCast: StartCastUseCase,
    private val playback: CastPlaybackUseCase,
    private val stopCast: StopCastUseCase,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {
    private var currentUri: String = savedStateHandle.get<String>(KEY_CURRENT_URI)
        ?: savedStateHandle.toRoute<Route.CastVideo>().uri
    private val _uiState = MutableStateFlow(CastVideoUiState())
    val uiState: StateFlow<CastVideoUiState> = _uiState.asStateFlow()
    private val _events = Channel<CastVideoEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val commandMutex = Mutex()
    private var castJob: Job? = null
    private var statusJob: Job? = null
    private var thumbnailJob: Job? = null
    private var isStopping = false

    init {
        castMedia()
        loadThumbnail()
    }

    private fun castMedia(countForAds: Boolean = true) {
        castJob?.cancel()
        statusJob?.cancel()
        castJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    playerState = CastPlayerState.LOADING,
                    showLoadingMessage = true,
                )
            }
            try {
                val television =
                    getConnectedTelevision() ?: return@launch fail(CastFailReason.DEVICE_NOT_FOUND)
                val mediaInfo = getMediaInfo(currentUri)
                _uiState.update { it.copy(title = mediaInfo.title, tvName = television.name) }

                val media = CastMedia(
                    contentUri = currentUri,
                    type = CastMediaType.VIDEO,
                    title = mediaInfo.title,
                    mimeType = mediaInfo.mimeType,
                )

                startCast(television, media, countForAds).onSuccess { observe() }.onFailure { error ->
                    ensureActive()
                    Log.e(TAG, "Failed to cast media to ${television.ipAddress}", error)
                    fail(failReason(error))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to prepare media for casting", e)
                fail(failReason(e))
            }
        }
    }

    private fun loadThumbnail() {
        thumbnailJob?.cancel()
        val uri = currentUri
        thumbnailJob = viewModelScope.launch {
            val bytes = try {
                getVideoThumbnail(uri)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Thumbnail unavailable", e)
                null
            }
            if (uri != currentUri) return@launch
            _uiState.update { it.copy(thumbnailBytes = bytes) }
        }
    }

    private fun observe() {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            playback.status.collect { status ->
                _uiState.update {
                    it.copy(
                        playerState = status.state,
                        showLoadingMessage = status.state == CastPlayerState.LOADING && it.showLoadingMessage,
                        positionMs = status.positionMs,
                        durationMs = status.durationMs,
                        volume = status.volume,
                        isMuted = status.isMuted,
                    )
                }
                if (status.state == CastPlayerState.ERROR) {
                    Log.e(TAG, "Cast playback failed: ${status.errorMessage}")
                    fail(CastFailReason.UNKNOWN)
                }
            }
        }
    }

    fun onPlayPauseClick() = withCast {
        if (_uiState.value.playerState == CastPlayerState.PLAYING) {
            playback.pause()
        } else {
            playback.play()
        }
    }

    fun onRewindClick() = withCast { playback.seekBy(-SEEK_STEP_MS) }
    fun onForwardClick() = withCast { playback.seekBy(SEEK_STEP_MS) }
    fun onVolumeUpClick() = withCast(dropIfBusy = true) { playback.changeVolume(VOLUME_STEP) }
    fun onVolumeDownClick() = withCast(dropIfBusy = true) { playback.changeVolume(-VOLUME_STEP) }
    fun onMuteClick() = withCast { playback.setMuted(!_uiState.value.isMuted) }
    fun onSeekTo(positionMs: Long) = withCast { playback.seekTo(positionMs) }
    fun onRetryCasting() {
        castMedia(countForAds = false)
    }

    fun onChangeVideo(uri: Uri) {
        val selected = uri.toString()
        if (selected == currentUri) return
        currentUri = selected
        savedStateHandle[KEY_CURRENT_URI] = selected
        _uiState.update { it.copy(thumbnailBytes = null) }

        loadThumbnail()
        castMedia()
    }

    fun onStopCastingClick() {
        if (isStopping) return
        isStopping = true
        castJob?.cancel()
        statusJob?.cancel()
        viewModelScope.launch {
            val stopResult = stopCast()
            stopResult.onFailure { error -> Log.e(TAG, "Failed to stop casting", error) }
            val showInterstitial = if (stopResult.isSuccess) {
                try {
                    stopCast.shouldShowInterstitialAfterStop()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to read cast count for interstitial", e)
                    false
                }
            } else {
                false
            }
            _events.send(
                CastVideoEvent.StopCompleted(
                    showInterstitial = showInterstitial,
                )
            )
        }
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
                    _events.send(CastVideoEvent.ShowMessage(failReason(error)))
                }
            } finally {
                commandMutex.unlock()
            }
        }
    }

    private fun failReason(error: Throwable): CastFailReason =
        (error as? CastException)?.reason ?: CastFailReason.UNKNOWN

    private fun fail(reason: CastFailReason) {
        statusJob?.cancel()
        _uiState.update {
            it.copy(
                playerState = CastPlayerState.ERROR,
                showLoadingMessage = false,
            )
        }
        _events.trySend(CastVideoEvent.CastFailed(reason))
    }

    override fun onCleared() {
        appScope.launch {
            playback.stop().onFailure { error ->
                Log.e(TAG, "Failed to stop casting", error)
            }
            stopCast()
        }
        super.onCleared()
    }

    private companion object {
        const val TAG = "CastVideoViewModel"
        const val SEEK_STEP_MS = 10_000L
        const val VOLUME_STEP = 5
        const val KEY_CURRENT_URI = "current_uri"
    }
}