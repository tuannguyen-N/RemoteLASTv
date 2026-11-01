package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.Television
import jakarta.inject.Inject

class StartCastUseCase @Inject constructor(
    private val sessionManager: CastSessionManager,
) {
    suspend operator fun invoke(television: Television, media: CastMedia): Result<Unit> =
        sessionManager.start(television, media)
}
