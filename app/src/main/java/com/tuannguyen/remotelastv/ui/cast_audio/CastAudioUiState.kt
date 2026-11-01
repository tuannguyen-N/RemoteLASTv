package com.tuannguyen.remotelastv.ui.cast_audio

import com.tuannguyen.remotelastv.domain.model.AudioTrack
import com.tuannguyen.remotelastv.domain.model.CastPlayerState
import com.tuannguyen.remotelastv.domain.model.CastRepeatMode

data class CastAudioUiState(
    val title: String = "",
    val tvName: String = "",
    val playerState: CastPlayerState = CastPlayerState.IDLE,
    val showLoadingMessage: Boolean = false,
    val isLibraryLoading: Boolean = true,
    val isMusicPickerVisible: Boolean = true,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Int? = null,
    val isMuted: Boolean = false,
    val tracks: List<AudioTrack> = emptyList(),
    val currentTrackUri: String? = null,
    val playbackOrder: List<String> = emptyList(),
    val repeatMode: CastRepeatMode = CastRepeatMode.OFF,
    val isShuffleEnabled: Boolean = false,
)