package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.domain.model.CastProviderId
import com.tuannguyen.remotelastv.domain.model.CastStatus
import com.tuannguyen.remotelastv.domain.model.InterstitialAdPolicy
import com.tuannguyen.remotelastv.domain.model.PreparedCastMedia
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.repository.CastAdCounterRepository
import com.tuannguyen.remotelastv.domain.repository.CastProvider
import com.tuannguyen.remotelastv.domain.repository.CastSession
import com.tuannguyen.remotelastv.domain.repository.MediaPreparer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CastSessionManagerTest {
    @Test
    fun startSelectsHighestPriorityProviderAndReleasesMediaOnStop() = runBlocking {
        val dlnaSession = FakeCastSession()
        val googleSession = FakeCastSession()
        val preparer = FakeMediaPreparer()
        val manager = CastSessionManager(
            providers = setOf(
                FakeCastProvider(CastProviderId.DLNA, priority = 0, dlnaSession),
                FakeCastProvider(CastProviderId.GOOGLE_CAST, priority = 10, googleSession),
            ),
            mediaPreparer = preparer,
            castAdCounter = FakeCastAdCounterRepository(),
        )

        assertTrue(manager.start(television, media).isSuccess)
        assertFalse(dlnaSession.wasLoaded)
        assertTrue(googleSession.wasLoaded)
        assertEquals(CastProviderId.GOOGLE_CAST, preparer.preparedFor)

        assertTrue(manager.stop().isSuccess)
        assertTrue(googleSession.wasStopped)
        assertTrue(preparer.preparedMedia.released)
    }

    @Test
    fun failedLoadStopsSessionAndReleasesPreparedMedia() = runBlocking {
        val session = FakeCastSession(loadResult = Result.failure(IllegalStateException("load failed")))
        val preparer = FakeMediaPreparer()
        val manager = CastSessionManager(
            providers = setOf(FakeCastProvider(CastProviderId.DLNA, priority = 0, session)),
            mediaPreparer = preparer,
            castAdCounter = FakeCastAdCounterRepository(),
        )

        val result = manager.start(television, media)

        assertTrue(result.isFailure)
        assertTrue(session.wasStopped)
        assertTrue(preparer.preparedMedia.released)
    }

    @Test
    fun interstitialCounterCountsSuccessfulCastsButNotRetriesAndResetsAfterShown() = runBlocking {
        val counter = FakeCastAdCounterRepository()
        val manager = CastSessionManager(
            providers = setOf(FakeCastProvider(CastProviderId.DLNA, priority = 0, FakeCastSession())),
            mediaPreparer = FakeMediaPreparer(),
            castAdCounter = counter,
        )

        assertTrue(manager.start(television, media).isSuccess)
        assertTrue(manager.start(television, media, countForAds = false).isSuccess)
        assertTrue(manager.start(television, media).isSuccess)
        assertFalse(manager.shouldShowInterstitialAfterStop())

        assertTrue(manager.start(television, media).isSuccess)
        assertTrue(manager.shouldShowInterstitialAfterStop())

        val recreatedManager = CastSessionManager(
            providers = setOf(FakeCastProvider(CastProviderId.DLNA, priority = 0, FakeCastSession())),
            mediaPreparer = FakeMediaPreparer(),
            castAdCounter = counter,
        )
        assertTrue(recreatedManager.shouldShowInterstitialAfterStop())

        manager.onInterstitialShown()
        assertFalse(recreatedManager.shouldShowInterstitialAfterStop())
        assertTrue(recreatedManager.start(television, media).isSuccess)
        assertTrue(recreatedManager.start(television, media).isSuccess)
        assertFalse(recreatedManager.shouldShowInterstitialAfterStop())
        assertTrue(recreatedManager.start(television, media).isSuccess)
        assertFalse(recreatedManager.shouldShowInterstitialAfterStop())
        counter.advanceTime(InterstitialAdPolicy.COOLDOWN_MILLIS)
        assertTrue(recreatedManager.shouldShowInterstitialAfterStop())
    }

    private class FakeCastProvider(
        override val id: CastProviderId,
        override val priority: Int,
        private val session: FakeCastSession,
    ) : CastProvider {
        override fun supports(television: Television) = true

        override suspend fun createSession(television: Television): CastSession = session
    }

    private class FakeCastSession(
        private val loadResult: Result<Unit> = Result.success(Unit),
    ) : CastSession {
        private val mutableStatus = MutableStateFlow(CastStatus())
        override val status: StateFlow<CastStatus> = mutableStatus
        var wasLoaded = false
            private set
        var wasStopped = false
            private set

        override suspend fun load(media: CastMedia): Result<Unit> {
            wasLoaded = true
            return loadResult
        }

        override suspend fun play() = Result.success(Unit)
        override suspend fun pause() = Result.success(Unit)
        override suspend fun stop(): Result<Unit> {
            wasStopped = true
            return Result.success(Unit)
        }

        override suspend fun seekTo(positionMs: Long) = Result.success(Unit)
        override suspend fun seekBy(deltaMs: Long) = Result.success(Unit)
        override suspend fun changeVolume(delta: Int) = Result.success(Unit)
        override suspend fun setMuted(muted: Boolean) = Result.success(Unit)
    }

    private class FakeMediaPreparer : MediaPreparer {
        var preparedFor: CastProviderId? = null
        val preparedMedia = FakePreparedCastMedia(media)

        override suspend fun prepare(media: CastMedia, providerId: CastProviderId): PreparedCastMedia {
            preparedFor = providerId
            return preparedMedia
        }
    }

    private class FakeCastAdCounterRepository : CastAdCounterRepository {
        private var successfulCastCount = 0
        private var currentTimeMillis = 0L
        private var lastInterstitialShownAt: Long? = null

        override suspend fun recordSuccessfulCast() {
            successfulCastCount++
        }

        override suspend fun shouldShowInterstitial(
            threshold: Int,
            cooldownMillis: Long,
        ): Boolean {
            val isOutsideCooldown = lastInterstitialShownAt?.let {
                currentTimeMillis - it >= cooldownMillis
            } ?: true
            return successfulCastCount >= threshold && isOutsideCooldown
        }

        override suspend fun recordInterstitialShown() {
            successfulCastCount = 0
            lastInterstitialShownAt = currentTimeMillis
        }

        fun advanceTime(millis: Long) {
            currentTimeMillis += millis
        }
    }

    private class FakePreparedCastMedia(
        override val media: CastMedia,
    ) : PreparedCastMedia {
        var released = false
            private set

        override suspend fun release() {
            released = true
        }
    }

    private companion object {
        val television = Television("tv-id", "192.168.1.25", "Living room")
        val media = CastMedia("content://media/video", CastMediaType.VIDEO, "video.mp4")
    }
}
