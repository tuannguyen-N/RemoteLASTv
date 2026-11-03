package com.tuannguyen.remotelastv.data.tv.samsung

import com.tuannguyen.remotelastv.data.tv.AppCatalog
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TouchpadEvent
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SamsungTvController @Inject constructor(
    private val registry: SamsungSessionRegistry,
) : TvControllerRepository {
    private enum class Probe { INSTALLED, MISSING, UNKNOWN }

    override val capabilities: Set<TvCapability> = setOf()

    override val touchpadMode: TouchpadMode = TouchpadMode.DIRECTIONAL

    private val probeClient = OkHttpClient.Builder()
        .callTimeout(2, TimeUnit.SECONDS)
        .build()

    private val resolvedIds = ConcurrentHashMap<KnownApp, String>()
    @Volatile
    private var resolvedForTvId: String? = null

    override suspend fun sendKey(key: TvKey): Result<Unit> {
        val code = KEY_MAP[key]
            ?: return Result.failure(UnsupportedOperationException("Samsung does not support $key"))
        return registry.sendKey(code)
    }

    override suspend fun getApps(): Result<List<KnownApp>> {
        return Result.success(
            KnownApp.entries.filter { app ->
                AppCatalog.targetsOf(app, TelevisionBrand.SAMSUNG).isNotEmpty()
            }
        )
    }

    override suspend fun launchApp(app: KnownApp): Result<Unit> {
        val tv = registry.activeTv
            ?: return Result.failure(IllegalStateException("Samsung TV not connected"))

        val candidates = AppCatalog.targetsOf(app, TelevisionBrand.SAMSUNG)
        if (candidates.isEmpty()) {
            return Result.failure(UnsupportedOperationException("${app.name} not available on Samsung"))
        }

        val appId = resolveAppId(tv, app, candidates)
            ?: return Result.failure(Error("App not installed"))

        return registry.launchApp(appId)
    }

    override suspend fun sendTouchpad(event: TouchpadEvent): Result<Unit> = when (event) {
        is TouchpadEvent.Move -> registry.moveCursor(event.dx.roundToInt(), event.dy.roundToInt())
        TouchpadEvent.Tap -> registry.mouseClick()
        else -> Result.success(Unit)
    }

    private suspend fun resolveAppId(
        tv: Television,
        app: KnownApp,
        candidates: List<String>,
    ): String? {
        if (resolvedForTvId != tv.id) {
            resolvedIds.clear()
            resolvedForTvId = tv.id
        }
        resolvedIds[app]?.let { return it }

        val results = withContext(Dispatchers.IO) {
            candidates.associateWith { probe(tv.ipAddress, it) }
        }

        results.entries.firstOrNull { it.value == Probe.INSTALLED }?.key?.let {
            resolvedIds[app] = it
            return it
        }
        results.entries.firstOrNull { it.value == Probe.UNKNOWN }?.key?.let { return it }

        return null
    }

    private fun probe(ip: String, appId: String): Probe = try {
        val request = Request.Builder()
            .url("http://$ip:8001/api/v2/applications/$appId")
            .build()
        probeClient.newCall(request).execute().use {
            when (it.code) {
                200 -> Probe.INSTALLED
                404 -> Probe.MISSING
                else -> Probe.UNKNOWN
            }
        }
    } catch (_: IOException) {
        Probe.UNKNOWN
    }

    private companion object {
        val KEY_MAP: Map<TvKey, String> = mapOf(
            TvKey.UP to "KEY_UP",
            TvKey.DOWN to "KEY_DOWN",
            TvKey.LEFT to "KEY_LEFT",
            TvKey.RIGHT to "KEY_RIGHT",
            TvKey.OK to "KEY_ENTER",
            TvKey.BACK to "KEY_RETURN",
            TvKey.HOME to "KEY_HOME",
            TvKey.MENU to "KEY_MENU",
            TvKey.POWER to "KEY_POWER",
            TvKey.VOLUME_UP to "KEY_VOLUP",
            TvKey.VOLUME_DOWN to "KEY_VOLDOWN",
            TvKey.MUTE to "KEY_MUTE",
            TvKey.CHANNEL_UP to "KEY_CHUP",
            TvKey.CHANNEL_DOWN to "KEY_CHDOWN",
            TvKey.NUMBER_0 to "KEY_0",
            TvKey.NUMBER_1 to "KEY_1",
            TvKey.NUMBER_2 to "KEY_2",
            TvKey.NUMBER_3 to "KEY_3",
            TvKey.NUMBER_4 to "KEY_4",
            TvKey.NUMBER_5 to "KEY_5",
            TvKey.NUMBER_6 to "KEY_6",
            TvKey.NUMBER_7 to "KEY_7",
            TvKey.NUMBER_8 to "KEY_8",
            TvKey.NUMBER_9 to "KEY_9",
            TvKey.PLAY_PAUSE to "KEY_PLAY",
            TvKey.PREVIOUS to "KEY_REWIND",
            TvKey.NEXT to "KEY_FF",
            TvKey.DELETE to "KEY_DELETE"
        )
    }
}