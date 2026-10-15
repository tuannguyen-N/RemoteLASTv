package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import kotlinx.coroutines.flow.Flow

interface TvDiscoveryRepository {
    val brand: TelevisionBrand
    fun startScan(): Flow<Television>
}