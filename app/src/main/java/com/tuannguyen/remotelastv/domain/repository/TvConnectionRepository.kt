package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.ConnectionStatus
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TvCredentials
import kotlinx.coroutines.flow.Flow

interface TvConnectionRepository {
    fun connect(television: Television, credentials: TvCredentials): Flow<ConnectionStatus>
    suspend fun disconnect()
}