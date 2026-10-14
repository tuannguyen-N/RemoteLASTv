package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.repository.SavedTvRepository
import com.tuannguyen.remotelastv.domain.repository.TvDiscoveryRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withTimeoutOrNull

class TvDiscoveryUseCase @Inject constructor(
    private val discoveries: Set<@JvmSuppressWildcards TvDiscoveryRepository>,
    private val savedTvRepository: SavedTvRepository
) {
    operator fun invoke(timeoutMs: Long = DEFAULT_TIMEOUT_MS): Flow<Television> = flow {
        val seen = mutableSetOf<String>()
        withTimeoutOrNull(timeoutMs) {
            discoveries
                .map { it.startScan().catch { } }
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
        const val DEFAULT_TIMEOUT_MS = 15_000L
    }
}