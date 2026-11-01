package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.repository.TvDiscovery
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge

class DiscoverTelevisionsUseCase @Inject constructor(
    private val discoveries: Set<@JvmSuppressWildcards TvDiscovery>,
) {
    operator fun invoke(timeoutMs: Long = 5_000, hintHost: String? = null): Flow<Television> =
        flow {
            val seenHosts = HashSet<String>()
            merge(*discoveries.map { it.discover(timeoutMs, hintHost) }.toTypedArray())
                .collect { television ->
                    if (seenHosts.add(television.ipAddress)) emit(television)
                }
        }
}
