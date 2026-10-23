package com.tuannguyen.remotelastv.domain.manager

import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.VolumeState
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvControllerManager @Inject constructor(
    private val repositories: Map<TelevisionBrand, @JvmSuppressWildcards TvControllerRepository>,
    private val connectionManager: TvConnectionManager
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val capabilities: Flow<Set<TvCapability>> = connectionManager.connectedTelevision
        .map { tv -> tv?.let { repositories[it.brand]?.capabilities }.orEmpty() }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val volume: Flow<VolumeState?> = connectionManager.connectedTelevision
        .flatMapLatest { tv ->
            val repo = tv?.let { repositories[it.brand] }
            if (repo == null || TvCapability.VOLUME_STATE !in repo.capabilities) {
                flowOf(null)
            } else {
                repo.observeVolume()
                    .map<VolumeState, VolumeState?> { it }
                    .onStart { emit(null) }
                    .catch { emit(null) }
            }
        }
        .distinctUntilChanged()

    private fun current(): TvControllerRepository? =
        connectionManager.connectedTelevision.value?.let { repositories[it.brand] }

    val touchpadMode: Flow<TouchpadMode> = connectionManager.connectedTelevision
        .map { tv -> tv?.let { repositories[it.brand]?.touchpadMode } ?: TouchpadMode.DIRECTIONAL }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val voiceRecording: Flow<Boolean> = connectionManager.connectedTelevision
        .flatMapLatest { tv ->
            tv?.let { repositories[it.brand]?.observeVoiceRecording() } ?: flowOf(false)
        }
        .distinctUntilChanged()

    suspend fun <T> execute(
        block: suspend (TvControllerRepository) -> Result<T>
    ): Result<T> {
        val controller = current() ?: return Result.failure(TvException.NotConnected())
        return block(controller)
    }
}