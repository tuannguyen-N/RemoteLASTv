package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.model.Television
import jakarta.inject.Inject

class GetConnectedTelevisionUseCase @Inject constructor(
    private val connectionManager: TvConnectionManager,
) {
    operator fun invoke(): Television? = connectionManager.connectedTelevision.value
}
