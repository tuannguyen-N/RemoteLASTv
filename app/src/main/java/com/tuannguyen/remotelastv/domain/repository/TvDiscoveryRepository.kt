package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.Television
import kotlinx.coroutines.flow.Flow

interface TvDiscoveryRepository {
    fun startScan(): Flow<Television>
    fun stopScan()
}