package com.tuannguyen.remotelastv.ui.cast_video

data class CastVideoUiState(
    val title: String = "",
    val tvName: String = "",
    val thumbnailBytes: ByteArray? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Int? = null,
    val isMuted: Boolean = false,
)