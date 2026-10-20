package com.tuannguyen.remotelastv.data.tv.android_tv

import android.view.KeyEvent
import com.tuannguyen.remotelastv.data.tv.AppCatalog
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.model.VolumeState
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class AndroidTvTController @Inject constructor(
    private val remoteClient: RemoteClient,
) : TvControllerRepository {
    override val capabilities: Set<TvCapability> = setOf(TvCapability.VOLUME_STATE, TvCapability.VOICE)
    override val touchpadMode: TouchpadMode get() = TouchpadMode.DIRECTIONAL

    override suspend fun sendKey(key: TvKey): Result<Unit> {
        if (!remoteClient.isConnected) {
            return Result.failure(TvException.NotConnected())
        }

        return try {
            remoteClient.sendKey(key.toAndroidKeyCode())
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(TvException.CommandFailed(e))
        }
    }

    override suspend fun getApps(): Result<List<KnownApp>> {
        return Result.success(
            KnownApp.entries.filter { app ->
                AppCatalog.targetsOf(app, TelevisionBrand.ANDROID_TV).isNotEmpty()
            }
        )
    }

    override suspend fun launchApp(app: KnownApp): Result<Unit> {
        val appLink = AppCatalog.targetsOf(app, TelevisionBrand.ANDROID_TV).firstOrNull()
            ?: return Result.failure(TvException.AppNotAvailable(app))
        if (!remoteClient.isConnected) {
            return Result.failure(TvException.NotConnected())
        }

        return try {
            remoteClient.launchApp(appLink)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(TvException.CommandFailed(e))
        }
    }

    private fun TvKey.toAndroidKeyCode(): Int = when (this) {
        TvKey.UP -> KeyEvent.KEYCODE_DPAD_UP
        TvKey.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
        TvKey.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
        TvKey.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
        TvKey.OK -> KeyEvent.KEYCODE_DPAD_CENTER
        TvKey.BACK -> KeyEvent.KEYCODE_BACK
        TvKey.HOME -> KeyEvent.KEYCODE_HOME
        TvKey.MENU -> KeyEvent.KEYCODE_MENU
        TvKey.POWER -> KeyEvent.KEYCODE_POWER
        TvKey.VOLUME_UP -> KeyEvent.KEYCODE_VOLUME_UP
        TvKey.VOLUME_DOWN -> KeyEvent.KEYCODE_VOLUME_DOWN
        TvKey.MUTE -> KeyEvent.KEYCODE_VOLUME_MUTE
        TvKey.CHANNEL_UP -> KeyEvent.KEYCODE_CHANNEL_UP
        TvKey.CHANNEL_DOWN -> KeyEvent.KEYCODE_CHANNEL_DOWN
        TvKey.NUMBER_0 -> KeyEvent.KEYCODE_0
        TvKey.NUMBER_1 -> KeyEvent.KEYCODE_1
        TvKey.NUMBER_2 -> KeyEvent.KEYCODE_2
        TvKey.NUMBER_3 -> KeyEvent.KEYCODE_3
        TvKey.NUMBER_4 -> KeyEvent.KEYCODE_4
        TvKey.NUMBER_5 -> KeyEvent.KEYCODE_5
        TvKey.NUMBER_6 -> KeyEvent.KEYCODE_6
        TvKey.NUMBER_7 -> KeyEvent.KEYCODE_7
        TvKey.NUMBER_8 -> KeyEvent.KEYCODE_8
        TvKey.NUMBER_9 -> KeyEvent.KEYCODE_9
        TvKey.DELETE -> KeyEvent.KEYCODE_DEL
        TvKey.PREVIOUS -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
        TvKey.NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
        TvKey.PLAY_PAUSE -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
    }

    override fun observeVolume(): Flow<VolumeState> = remoteClient.volumeState
}