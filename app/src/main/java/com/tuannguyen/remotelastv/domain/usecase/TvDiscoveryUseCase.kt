package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.repository.TvDiscoveryRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withTimeoutOrNull

class TvDiscoveryUseCase @Inject constructor(
    private val discoveries: Set<@JvmSuppressWildcards TvDiscoveryRepository>
) {
    operator fun invoke(timeoutMs: Long = DEFAULT_TIMEOUT_MS): Flow<Television> = flow {
        val seen = mutableSetOf<String>()
        withTimeoutOrNull(timeoutMs) {
            discoveries
                .map { discovery ->
                    discovery.startScan().catch { /* một hãng lỗi không làm hỏng hãng khác */ }
                }
                .merge()
                .collect { tv ->
                    if (seen.add(tv.ipAddress)) emit(tv)
                }
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 15_000L
    }
}