package com.tuannguyen.remotelastv.data.tv.roku

import com.tuannguyen.remotelastv.domain.model.TextCapability
import com.tuannguyen.remotelastv.domain.model.TextInputState
import com.tuannguyen.remotelastv.domain.repository.TvTextInputRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class RokuTextInput @Inject constructor(
    private val client: OkHttpClient,
    private val registry: RokuRegistry,
) : TvTextInputRepository {

    override val capabilities: Set<TextCapability> = emptySet()

    override fun observeInput(): Flow<TextInputState> = flowOf(TextInputState.Unknown)

    override suspend fun sendText(text: String): Result<Unit> {
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            i += Character.charCount(cp)

            val ch = String(Character.toChars(cp))
            val encoded = withContext(Dispatchers.IO) {
                URLEncoder.encode(ch, "UTF-8")
            }.replace("+", "%20")
            post("keypress/Lit_$encoded").onFailure { return Result.failure(it) }
            delay(KEY_DELAY_MS.milliseconds)
        }
        return Result.success(Unit)
    }

    override suspend fun sendDelete(): Result<Unit> = post("keypress/Backspace")

    override suspend fun sendEnter(): Result<Unit> = post("keypress/Enter")

    override suspend fun replaceText(text: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Roku không hỗ trợ thay thế toàn bộ text"))

    private suspend fun post(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        val ip = registry.ip
            ?: return@withContext Result.failure(IllegalStateException("Chưa kết nối Roku"))
        runCatching {
            client.newCall(
                Request.Builder()
                    .url("http://$ip:8060/$path")
                    .post(ByteArray(0).toRequestBody())
                    .build()
            ).execute().use { check(it.isSuccessful) { "HTTP ${it.code}" } }
        }
    }

    private companion object {
        const val KEY_DELAY_MS = 30L
    }
}