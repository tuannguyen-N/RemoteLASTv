package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.controller.TvControllerManager
import com.tuannguyen.remotelastv.domain.model.KnownApp
import jakarta.inject.Inject

class GetTvAppsUseCase @Inject constructor(
    private val manager: TvControllerManager
) {
    suspend operator fun invoke(): Result<List<KnownApp>> =
        manager.execute { it.getApps() }
}