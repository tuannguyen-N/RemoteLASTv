package com.tuannguyen.remotelastv.domain.controller

import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvControllerManager @Inject constructor(
    private val repositories: Map<TelevisionBrand, @JvmSuppressWildcards TvControllerRepository>,
    private val connectionManager: TvConnectionManager
) {
    private fun current(): TvControllerRepository? =
        connectionManager.connectedTelevision.value?.let { repositories[it.brand] }

    suspend fun execute(
        block: suspend (TvControllerRepository) -> Result<Unit>
    ): Result<Unit> {
        val controller = current() ?: return Result.failure(TvException.NotConnected())
        return block(controller)
    }
}