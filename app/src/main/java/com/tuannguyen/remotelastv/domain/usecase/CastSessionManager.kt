package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.model.CastException
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.CastStatus
import com.tuannguyen.remotelastv.domain.model.InterstitialAdPolicy
import com.tuannguyen.remotelastv.domain.model.PreparedCastMedia
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.repository.CastAdCounterRepository
import com.tuannguyen.remotelastv.domain.repository.CastProvider
import com.tuannguyen.remotelastv.domain.repository.CastSession
import com.tuannguyen.remotelastv.domain.repository.MediaPreparer
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class CastSessionManager @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards CastProvider>,
    private val mediaPreparer: MediaPreparer,
    private val castAdCounter: CastAdCounterRepository,
) {
    private data class ActiveCast(
        val session: CastSession,
        val preparedMedia: PreparedCastMedia,
    )

    private val lock = Mutex()
    private val activeCast = MutableStateFlow<ActiveCast?>(null)

    val status: Flow<CastStatus> = activeCast.flatMapLatest { active ->
        active?.session?.status ?: flowOf(CastStatus())
    }

    suspend fun start(
        television: Television,
        media: CastMedia,
        countForAds: Boolean = true,
    ): Result<Unit> =
        try {
            lock.withLock {
                stopActiveCast()?.let { return@withLock Result.failure(it) }

                val provider = providers
                    .asSequence()
                    .filter { it.supports(television) }
                    .maxByOrNull(CastProvider::priority)
                    ?: throw CastException(CastFailReason.NOT_SUPPORTED)
                val session = provider.createSession(television)
                val prepared = try {
                    mediaPreparer.prepare(media, provider.id)
                } catch (e: CancellationException) {
                    cleanupFailedCast(session, null, e)
                    throw e
                } catch (e: Exception) {
                    cleanupFailedCast(session, null, e)
                    throw e
                }

                val loadResult = try {
                    session.load(prepared.media)
                } catch (e: CancellationException) {
                    cleanupFailedCast(session, prepared, e)
                    throw e
                } catch (e: Exception) {
                    cleanupFailedCast(session, prepared, e)
                    throw e
                }

                val loadError = loadResult.exceptionOrNull()
                if (loadError != null) {
                    cleanupFailedCast(session, prepared, loadError)
                    return@withLock Result.failure(loadError)
                }
                if (countForAds) {
                    try {
                        castAdCounter.recordSuccessfulCast()
                    } catch (e: CancellationException) {
                        cleanupFailedCast(session, prepared, e)
                        throw e
                    } catch (e: Exception) {
                        cleanupFailedCast(session, prepared, e)
                        throw e
                    }
                }
                activeCast.value = ActiveCast(session, prepared)
                Result.success(Unit)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun stop(): Result<Unit> = lock.withLock {
        stopActiveCast()?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    suspend fun shouldShowInterstitialAfterStop(): Boolean =
        castAdCounter.shouldShowInterstitial(
            threshold = INTERSTITIAL_CAST_THRESHOLD,
            cooldownMillis = InterstitialAdPolicy.COOLDOWN_MILLIS,
        )

    suspend fun onInterstitialShown() =
        castAdCounter.recordInterstitialShown()

    suspend fun play(): Result<Unit> = withActiveSession { it.play() }
    suspend fun pause(): Result<Unit> = withActiveSession { it.pause() }
    suspend fun seekTo(positionMs: Long): Result<Unit> = withActiveSession { it.seekTo(positionMs) }
    suspend fun seekBy(deltaMs: Long): Result<Unit> = withActiveSession { it.seekBy(deltaMs) }
    suspend fun changeVolume(delta: Int): Result<Unit> =
        withActiveSession { it.changeVolume(delta) }

    suspend fun setMuted(muted: Boolean): Result<Unit> = withActiveSession { it.setMuted(muted) }

    private suspend fun withActiveSession(
        command: suspend (CastSession) -> Result<Unit>,
    ): Result<Unit> = lock.withLock {
        val session = activeCast.value?.session
            ?: return@withLock Result.failure(CastException(CastFailReason.DISCONNECTED))
        try {
            command(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun stopActiveCast(): Throwable? {
        val active = activeCast.value ?: return null
        activeCast.value = null
        return withContext(NonCancellable) {
            var failure: Throwable? = null
            var cancellation: CancellationException? = null
            try {
                active.session.stop().exceptionOrNull()?.let { failure = it }
            } catch (e: CancellationException) {
                cancellation = e
            } catch (e: Exception) {
                failure = e
            }
            try {
                active.preparedMedia.release()
            } catch (e: CancellationException) {
                val previousCancellation = cancellation
                if (previousCancellation == null) cancellation =
                    e else previousCancellation.addSuppressed(e)
            } catch (e: Exception) {
                val previousFailure = failure
                if (previousFailure == null) failure = e else previousFailure.addSuppressed(e)
            }
            cancellation?.let { throw it }
            failure
        }
    }

    private suspend fun cleanupFailedCast(
        session: CastSession,
        prepared: PreparedCastMedia?,
        cause: Throwable,
    ) = withContext(NonCancellable) {
        prepared?.let {
            try {
                it.release()
            } catch (e: Exception) {
                cause.addSuppressed(e)
            }
        }
        try {
            session.stop().exceptionOrNull()?.let(cause::addSuppressed)
        } catch (e: Exception) {
            cause.addSuppressed(e)
        }
    }

    private companion object {
        const val INTERSTITIAL_CAST_THRESHOLD = 3
    }
}
