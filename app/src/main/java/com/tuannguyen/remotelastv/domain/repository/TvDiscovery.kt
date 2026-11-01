package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.Television
import kotlinx.coroutines.flow.Flow

interface TvDiscovery {
    fun discover(timeoutMs: Long = 5_000, hintHost: String? = null): Flow<Television>
}
