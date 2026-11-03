package com.tuannguyen.remotelastv.domain.usecase

import android.util.Log
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.repository.SavedTvRepository
import com.tuannguyen.remotelastv.domain.repository.TvDiscoveryRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class TvDiscoveryUseCase @Inject constructor(
    private val discoveries: Set<@JvmSuppressWildcards TvDiscoveryRepository>,
    private val savedTvRepository: SavedTvRepository
) {
    operator fun invoke(timeoutMs: Long = DEFAULT_TIMEOUT_MS): Flow<Television> = flow {
        val seen = mutableSetOf<String>()
        withTimeoutOrNull(timeoutMs.milliseconds) {
            discoveries
                .map { discovery ->
                    discovery.startScan().catch { error ->
                        Log.e(TAG, "TV discovery failed for ${discovery.brand}", error)
                    }
                }
                .merge()
                .collect { tv ->
                    if (seen.add(tv.ipAddress)) {
                        val savedName = savedTvRepository.getCustomName(tv.id)
                        emit(if (savedName != null) tv.copy(name = savedName) else tv)
                    }
                }
        }
    }

    private companion object {
        const val TAG = "TvDiscoveryUseCase"
        const val DEFAULT_TIMEOUT_MS = 15_000L
    }
}