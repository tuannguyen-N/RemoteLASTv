package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.model.CastStatus
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class CastPlaybackUseCase @Inject constructor(
    private val sessionManager: CastSessionManager,
) {
    val status: Flow<CastStatus> = sessionManager.status

    suspend fun play() = sessionManager.play()
    suspend fun pause() = sessionManager.pause()
    suspend fun stop() = sessionManager.stop()
    suspend fun seekTo(positionMs: Long) = sessionManager.seekTo(positionMs)
    suspend fun seekBy(deltaMs: Long) = sessionManager.seekBy(deltaMs)
    suspend fun changeVolume(delta: Int) = sessionManager.changeVolume(delta)
    suspend fun setMuted(muted: Boolean) = sessionManager.setMuted(muted)
}
