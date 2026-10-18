package com.tuannguyen.remotelastv.data.tv.lg

import android.util.Log
import com.connectsdk.core.AppInfo
import com.connectsdk.service.WebOSTVService
import com.connectsdk.service.capability.KeyControl
import com.connectsdk.service.capability.Launcher
import com.connectsdk.service.capability.VolumeControl
import com.connectsdk.service.capability.listeners.ResponseListener
import com.connectsdk.service.command.ServiceCommandError
import com.tuannguyen.remotelastv.data.tv.AppCatalog
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TouchpadEvent
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
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
    override val touchpadMode: TouchpadMode
        get() = if (registry.supportsMouse()) TouchpadMode.POINTER else TouchpadMode.DIRECTIONAL


    @Volatile
    private var isPlaying = true

    @Volatile
    private var resolvedIds: Map<KnownApp, String> = emptyMap()

    ///--------------KEY----------------------

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

    ///--------------------APP----------------------

    override suspend fun launchApp(app: KnownApp): Result<Unit> {
        val webOs = registry.activeDevice?.getServiceByName(WebOSTVService.ID) as? WebOSTVService

        val id = resolvedIds[app]
            ?: AppCatalog.targetsOf(app, TelevisionBrand.LG).firstOrNull()
            ?: return Result.failure(TvException.AppNotAvailable(app))
        return runCatching {
            webOs?.launcher?.launchApp(id, null)
        }
    }

    override suspend fun getApps(): Result<List<KnownApp>> {
        resolvedIds = emptyMap()
        val device = registry.activeDevice?.takeIf { it.isConnected }
            ?: return Result.failure(TvException.NotConnected())
        val launcher = device.getCapability(Launcher::class.java)
            ?: return Result.failure(UnsupportedOperationException("getApps"))

        return fetchRawApps(launcher).map { raw ->
            resolvedIds = raw.mapNotNull { info ->
                AppCatalog.identify(info.id, TelevisionBrand.LG)?.let { it to info.id }
            }.toMap()
            resolvedIds.keys.toList()
        }
    }

    private suspend fun fetchRawApps(launcher: Launcher): Result<List<AppInfo>> =
        withTimeoutOrNull(GET_APPS_TIMEOUT_MS) {
            suspendCancellableCoroutine<Result<List<AppInfo>>> { cont ->
                launcher.getAppList(object : Launcher.AppListListener {
                    override fun onSuccess(apps: List<AppInfo>) {
                        if (cont.isActive) cont.resume(Result.success(apps))
                    }

                    override fun onError(error: ServiceCommandError) {
                        if (cont.isActive) cont.resume(
                            Result.failure(
                                Exception(
                                    error.message,
                                    error
                                )
                            )
                        )
                    }
                })
            }
        } ?: Result.failure(TimeoutException("getApps timed out"))


    ///--------------------TOUCHPAD----------------------
    override suspend fun sendTouchpad(event: TouchpadEvent): Result<Unit> = runCatching {
        val mouse = registry.ensureMouse() ?: error("Mouse not supported")
        when (event) {
            is TouchpadEvent.Move -> mouse.move(event.dx.toDouble(), event.dy.toDouble())
            is TouchpadEvent.Scroll -> mouse.scroll(event.dx.toDouble(), event.dy.toDouble())
            TouchpadEvent.Tap -> mouse.click()
        }
    }

    ///--------------------HELPER----------------------


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
        awaitResult(timeoutMs, command)
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

    private companion object {
        const val GET_APPS_TIMEOUT_MS = 5_000L
    }
}