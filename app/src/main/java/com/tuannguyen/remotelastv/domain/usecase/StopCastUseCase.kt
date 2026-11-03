package com.tuannguyen.remotelastv.domain.usecase

import jakarta.inject.Inject

class StopCastUseCase @Inject constructor(
    private val sessionManager: CastSessionManager,
) {
    suspend operator fun invoke(): Result<Unit> =
        sessionManager.stop()

    suspend fun shouldShowInterstitialAfterStop(): Boolean =
        sessionManager.shouldShowInterstitialAfterStop()

    suspend fun onInterstitialShown() {
        sessionManager.onInterstitialShown()
    }
}