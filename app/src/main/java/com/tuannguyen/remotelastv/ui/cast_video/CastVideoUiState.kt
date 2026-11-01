package com.tuannguyen.remotelastv.ui.cast_video

import com.tuannguyen.remotelastv.domain.model.CastPlayerState

data class CastVideoUiState(
    val title: String = "",
    val tvName: String = "",
    val thumbnailBytes: ByteArray? = null,
    val playerState: CastPlayerState = CastPlayerState.LOADING,
    val showLoadingMessage: Boolean = true,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Int? = null,
    val isMuted: Boolean = false,
)