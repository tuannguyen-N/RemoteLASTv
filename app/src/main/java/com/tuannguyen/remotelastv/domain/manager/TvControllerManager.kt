package com.tuannguyen.remotelastv.domain.manager

import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvControllerManager @Inject constructor(
    private val repositories: Map<TelevisionBrand, @JvmSuppressWildcards TvControllerRepository>,
    private val connectionManager: TvConnectionManager
) {
    private fun current(): TvControllerRepository? =
        connectionManager.connectedTelevision.value?.let { repositories[it.brand] }

    val touchpadMode: Flow<TouchpadMode> = connectionManager.connectedTelevision
        .map { tv -> tv?.let { repositories[it.brand]?.touchpadMode } ?: TouchpadMode.DIRECTIONAL }
        .distinctUntilChanged()

    suspend fun <T> execute(
        block: suspend (TvControllerRepository) -> Result<T>
    ): Result<T> {
        val controller = current() ?: return Result.failure(TvException.NotConnected())
        return block(controller)
    }
}