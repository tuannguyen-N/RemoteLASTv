package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.CastStatus
import kotlinx.coroutines.flow.StateFlow

interface CastSession {
    val status: StateFlow<CastStatus>

    suspend fun load(media: CastMedia): Result<Unit>
    suspend fun play(): Result<Unit>
    suspend fun pause(): Result<Unit>
    suspend fun stop(): Result<Unit>
    suspend fun seekTo(positionMs: Long): Result<Unit>
    suspend fun seekBy(deltaMs: Long): Result<Unit>
    suspend fun changeVolume(delta: Int): Result<Unit>
    suspend fun setMuted(muted: Boolean): Result<Unit>
}
