package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.CastProviderId
import com.tuannguyen.remotelastv.domain.model.Television

interface CastProvider {
    val id: CastProviderId
    val priority: Int

    fun supports(television: Television): Boolean

    suspend fun createSession(television: Television): CastSession
}