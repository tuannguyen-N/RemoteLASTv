package com.tuannguyen.remotelastv.domain.model

data class CastStatus(
    val state: CastPlayerState = CastPlayerState.IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Int? = null,
    val isMuted: Boolean = false,
    val errorMessage: String? = null,
)