package com.tuannguyen.remotelastv.domain.manager

import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TextCapability
import com.tuannguyen.remotelastv.domain.model.TextInputState
import com.tuannguyen.remotelastv.domain.repository.TvTextInputRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class TvTextInputManager @Inject constructor(
    private val repositories: Map<TelevisionBrand, @JvmSuppressWildcards TvTextInputRepository>,
    private val connectionManager: TvConnectionManager,
) {
    private fun current(): TvTextInputRepository? =
        connectionManager.connectedTelevision.value?.let { repositories[it.brand] }

    @OptIn(ExperimentalCoroutinesApi::class)
    val inputState: Flow<TextInputState> = connectionManager.connectedTelevision
        .flatMapLatest { tv ->
            tv?.let { repositories[it.brand]?.observeInput() }
                ?: flowOf(TextInputState.Unknown)
        }
        .distinctUntilChanged()

    val capabilities: Flow<Set<TextCapability>> = connectionManager.connectedTelevision
        .map { tv -> tv?.let { repositories[it.brand]?.capabilities }.orEmpty() }
        .distinctUntilChanged()

    suspend fun <T> execute(
        block: suspend (TvTextInputRepository) -> Result<T>
    ): Result<T> {
        val repo = current() ?: return Result.failure(TvException.NotConnected())
        return block(repo)
    }
}