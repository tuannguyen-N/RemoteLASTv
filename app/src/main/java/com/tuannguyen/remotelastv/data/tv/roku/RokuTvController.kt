package com.tuannguyen.remotelastv.data.tv.roku

import com.tuannguyen.remotelastv.data.tv.AppCatalog
import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RokuTvController @Inject constructor(
    private val client: OkHttpClient,
    private val registry: RokuRegistry,
) : TvControllerRepository {

    override val capabilities: Set<TvCapability> = setOf()
    override val touchpadMode: TouchpadMode get() = TouchpadMode.DIRECTIONAL
    @Volatile private var installedIds: Set<String> = emptySet()

    override suspend fun sendKey(key: TvKey): Result<Unit> {
        val rokuKey = key.toRokuKey()
            ?: return Result.failure(UnsupportedOperationException("Unsupported key $key"))
        return post("keypress/$rokuKey")
    }

    override suspend fun getApps(): Result<List<KnownApp>> = withContext(Dispatchers.IO) {
        val ip = registry.ip ?: return@withContext Result.failure(IllegalStateException("No TV"))
        runCatching {
            val xml = client.newCall(
                Request.Builder().url("http://$ip:8060/query/apps").build()
            ).execute().use { resp ->
                check(resp.isSuccessful) { "HTTP ${resp.code}" }
                resp.body?.string().orEmpty()
            }
            val ids = APP_REGEX.findAll(xml).map { it.groupValues[1] }.toSet()
            installedIds = ids
            ids.mapNotNull { AppCatalog.identify(it, TelevisionBrand.ROKU) }.distinct()
        }
    }


    override suspend fun launchApp(app: KnownApp): Result<Unit> {
        val id = AppCatalog.targetsOf(app, TelevisionBrand.ROKU).firstOrNull()
            ?: return Result.failure(UnsupportedOperationException("$app has no Roku id"))
        return post("launch/$id")
    }

    private suspend fun post(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        val ip = registry.ip ?: return@withContext Result.failure(IllegalStateException("No TV"))
        runCatching {
            client.newCall(
                Request.Builder()
                    .url("http://$ip:8060/$path")
                    .post(ByteArray(0).toRequestBody())
                    .build()
            ).execute().use { check(it.isSuccessful) { "HTTP ${it.code}" } }
        }
    }

    private fun TvKey.toRokuKey(): String? = when (this) {
        TvKey.UP -> "Up"
        TvKey.DOWN -> "Down"
        TvKey.LEFT -> "Left"
        TvKey.RIGHT -> "Right"
        TvKey.OK -> "Select"
        TvKey.BACK -> "Back"
        TvKey.HOME -> "Home"
        TvKey.MENU -> "Info"
        TvKey.POWER -> "Power"
        TvKey.VOLUME_UP -> "VolumeUp"
        TvKey.VOLUME_DOWN -> "VolumeDown"
        TvKey.MUTE -> "VolumeMute"
        TvKey.CHANNEL_UP -> "ChannelUp"
        TvKey.CHANNEL_DOWN -> "ChannelDown"
        TvKey.PREVIOUS -> "Rev"
        TvKey.NEXT -> "Fwd"
        TvKey.PLAY_PAUSE -> "Play"
        TvKey.DELETE -> "Backspace"
        TvKey.NUMBER_0 -> "Lit_0"
        TvKey.NUMBER_1 -> "Lit_1"
        TvKey.NUMBER_2 -> "Lit_2"
        TvKey.NUMBER_3 -> "Lit_3"
        TvKey.NUMBER_4 -> "Lit_4"
        TvKey.NUMBER_5 -> "Lit_5"
        TvKey.NUMBER_6 -> "Lit_6"
        TvKey.NUMBER_7 -> "Lit_7"
        TvKey.NUMBER_8 -> "Lit_8"
        TvKey.NUMBER_9 -> "Lit_9"
        TvKey.SEARCH -> "Search"
    }

    private companion object {
        val APP_REGEX = Regex("""<app id="(\d+)"[^>]*>([^<]*)</app>""")
    }
}