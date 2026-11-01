package com.tuannguyen.remotelastv.ui.cast_image

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tuannguyen.remotelastv.di.ApplicationScope
import com.tuannguyen.remotelastv.domain.model.CastException
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.domain.usecase.GetConnectedTelevisionUseCase
import com.tuannguyen.remotelastv.domain.usecase.GetMediaInfoUseCase
import com.tuannguyen.remotelastv.domain.usecase.StartCastUseCase
import com.tuannguyen.remotelastv.domain.usecase.StopCastUseCase
import com.tuannguyen.remotelastv.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CastImageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getConnectedTelevision: GetConnectedTelevisionUseCase,
    private val getMediaInfo: GetMediaInfoUseCase,
    private val startCast: StartCastUseCase,
    private val stopCast: StopCastUseCase,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CastImageUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CastImageEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentUri: String = savedStateHandle.get<String>(KEY_CURRENT_URI)
        ?: savedStateHandle.toRoute<Route.CastImage>().uri
    private var castJob: Job? = null

    init {
        castCurrent()
    }

    fun onImageSelected(uri: Uri) {
        val selected = uri.toString()
        if (selected == currentUri) return
        currentUri = selected
        savedStateHandle[KEY_CURRENT_URI] = selected
        castCurrent()
    }

    fun onRetryCasting() {
        castCurrent()
    }

    fun onStopCastingClick() {
        castJob?.cancel()
        _events.trySend(CastImageEvent.NavigateBack)
    }

    private fun castCurrent() {
        castJob?.cancel()
        castJob = viewModelScope.launch {
            val target = currentUri
            _uiState.update { it.copy(phase = CastImagePhase.LOADING) }
            try {
                val television = getConnectedTelevision()
                    ?: return@launch fail(CastFailReason.DEVICE_NOT_FOUND)
                val info = getMediaInfo(target)

                _uiState.update { state ->
                    val withTv = state.copy(tvName = television.name)
                    if (withTv.imageUri == null) {
                        withTv.copy(imageUri = target.toUri(), title = info.title)
                    } else withTv
                }

                val media = CastMedia(
                    contentUri = target,
                    type = CastMediaType.IMAGE,
                    title = info.title,
                    mimeType = info.mimeType,
                )

                startCast(television, media)
                    .onSuccess {
                        _uiState.update { state ->
                            state.copy(
                                imageUri = target.toUri(),
                                title = info.title,
                                phase = CastImagePhase.CASTING,
                            )
                        }
                    }
                    .onFailure { error ->
                        ensureActive()
                        fail(failReason(error))
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail(failReason(e))
            }
        }
    }

    private fun failReason(error: Throwable): CastFailReason =
        (error as? CastException)?.reason ?: CastFailReason.UNKNOWN

    private fun fail(reason: CastFailReason) {
        _uiState.update { it.copy(phase = CastImagePhase.IDLE) }
        _events.trySend(CastImageEvent.CastFailed(reason))
    }

    override fun onCleared() {
        appScope.launch { stopCast() }
        super.onCleared()
    }

    private companion object {
        const val KEY_CURRENT_URI = "current_uri"
    }
}