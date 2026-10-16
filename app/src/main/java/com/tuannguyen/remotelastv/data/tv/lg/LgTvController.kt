package com.tuannguyen.remotelastv.data.tv.lg

import android.util.Log
import com.connectsdk.service.WebOSTVService
import com.connectsdk.service.capability.KeyControl
import com.connectsdk.service.capability.VolumeControl
import com.connectsdk.service.capability.listeners.ResponseListener
import com.connectsdk.service.command.ServiceCommandError
import com.tuannguyen.remotelastv.domain.model.TvApp
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LgTvController @Inject constructor(
    private val registry: LgDeviceRegistry
) : TvControllerRepository {
    override val capabilities: Set<TvCapability> = emptySet()

    @Volatile
    private var isPlaying = true

    override suspend fun sendKey(key: TvKey): Result<Unit> {
        val webOs = registry.activeDevice
            ?.takeIf { it.isConnected }
            ?.getServiceByName(WebOSTVService.ID) as? WebOSTVService
            ?: return Result.failure(TvException.NotConnected())

        return safeCall {
            val keys = webOs.keyControl
            when (key) {
                TvKey.UP -> keys.up(logListener("UP"))
                TvKey.DOWN -> keys.down(null)
                TvKey.LEFT -> keys.left(null)
                TvKey.RIGHT -> keys.right(null)
//                TvKey.OK -> keys.ok(null)
                TvKey.OK -> webOs.keyControl.sendKeyCode(KeyControl.KeyCode.ENTER, null)
                TvKey.BACK -> keys.back(null)
                TvKey.HOME -> keys.home(null)
                TvKey.MENU -> sendSpecialKeyMethod.invoke(webOs, "MENU", null)
                TvKey.POWER -> awaitCommand { webOs.powerOff(it) }
                TvKey.MUTE -> {
                    val muted = awaitMute(webOs)
                    awaitCommand { webOs.setMute(!muted, it) }
                }

                TvKey.VOLUME_UP -> webOs.volumeUp(logListener("VOL_UP"))
                TvKey.VOLUME_DOWN -> webOs.volumeDown(null)
                TvKey.CHANNEL_UP -> webOs.channelUp(null)
                TvKey.CHANNEL_DOWN -> webOs.channelDown(null)
                TvKey.PLAY_PAUSE -> {
                    if (isPlaying) webOs.pause(null) else webOs.play(null)
                    isPlaying = !isPlaying
                }

                TvKey.PREVIOUS -> webOs.rewind(null)
                TvKey.NEXT -> webOs.fastForward(null)
                TvKey.NUMBER_0 -> sendDigit(webOs, 0)
                TvKey.NUMBER_1 -> sendDigit(webOs, 1)
                TvKey.NUMBER_2 -> sendDigit(webOs, 2)
                TvKey.NUMBER_3 -> sendDigit(webOs, 3)
                TvKey.NUMBER_4 -> sendDigit(webOs, 4)
                TvKey.NUMBER_5 -> sendDigit(webOs, 5)
                TvKey.NUMBER_6 -> sendDigit(webOs, 6)
                TvKey.NUMBER_7 -> sendDigit(webOs, 7)
                TvKey.NUMBER_8 -> sendDigit(webOs, 8)
                TvKey.NUMBER_9 -> sendDigit(webOs, 9)
                TvKey.DELETE -> webOs.sendDelete()
            }
        }
    }

    private fun sendDigit(webOs: WebOSTVService, digit: Int) {
        require(digit in 0..9)
        if (registry.keyboardFocused) {
            webOs.sendText(digit.toString())
        } else {
            sendSpecialKeyMethod.invoke(webOs, digit.toString(), logListener("NUM_$digit"))
        }
    }

    override suspend fun launchApp(channel: TvApp): Result<Unit> {
        TODO("Not yet implemented")
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: TvException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(TvException.CommandFailed(e))
        }

    private fun logListener(tag: String) = object : ResponseListener<Any?> {
        override fun onSuccess(response: Any?) {
            Log.d("LgTv", "$tag ok")
        }

        override fun onError(error: ServiceCommandError) {
            Log.e("LgTv", "$tag failed: code=${error.code} msg=${error.message}")
        }
    }

    private suspend fun <T> awaitResult(
        timeoutMs: Long = 3_000,
        command: (ResponseListener<T>) -> Unit,
    ): T = withTimeoutOrNull(timeoutMs) {
        suspendCancellableCoroutine<T> { cont ->
            command(object : ResponseListener<T> {
                override fun onSuccess(response: T) {
                    if (cont.isActive) cont.resume(response)
                }

                override fun onError(error: ServiceCommandError) {
                    if (cont.isActive) cont.resumeWithException(TvException.CommandFailed(error))
                }
            })
        }
    } ?: throw TvException.CommandFailed(TimeoutException("TV did not respond"))

    private suspend fun awaitCommand(
        timeoutMs: Long = 3_000,
        command: (ResponseListener<Any?>) -> Unit,
    ) {
        awaitResult<Any?>(timeoutMs, command)
    }

    private suspend fun awaitMute(webOs: WebOSTVService, timeoutMs: Long = 3_000): Boolean =
        withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine<Boolean> { cont ->
                webOs.getMute(object : VolumeControl.MuteListener {
                    override fun onSuccess(muted: Boolean) {
                        if (cont.isActive) cont.resume(muted)
                    }

                    override fun onError(error: ServiceCommandError) {
                        if (cont.isActive) cont.resumeWithException(TvException.CommandFailed(error))
                    }
                })
            }
        } ?: throw TvException.CommandFailed(TimeoutException("TV did not respond"))

    private val sendSpecialKeyMethod by lazy {
        WebOSTVService::class.java.getDeclaredMethod(
            "sendSpecialKey", String::class.java, ResponseListener::class.java
        ).apply { isAccessible = true }
    }
}