package com.tuannguyen.remotelastv.domain.repository

interface CastAdCounterRepository {
    suspend fun recordSuccessfulCast()
    suspend fun shouldShowInterstitial(threshold: Int, cooldownMillis: Long): Boolean
    suspend fun recordInterstitialShown()
}
