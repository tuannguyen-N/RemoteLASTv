package com.tuannguyen.remotelastv.data.tv.samsung

import android.util.Base64
import com.tuannguyen.remotelastv.domain.model.TextCapability
import com.tuannguyen.remotelastv.domain.model.TextInputState
import com.tuannguyen.remotelastv.domain.repository.TvTextInputRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class SamsungTvTextInput @Inject constructor(
    private val registry: SamsungSessionRegistry,
) : TvTextInputRepository {

    override val capabilities: Set<TextCapability> = setOf(
        TextCapability.OBSERVE_FOCUS,
        TextCapability.AUTO_SHOW_KEYBOARD_ON_FOCUS,
        TextCapability.REPLACE_TEXT,
    )

    @Volatile
    private var current = ""

    override fun observeInput(): Flow<TextInputState> = flow {
        emit(TextInputState.Unknown)
        registry.events.collect { event ->
            when (event.optString("event")) {
                "ms.remote.imeStart" -> {
                    current = ""
                    emit(TextInputState.Focused(text = null))
                }

                "ms.remote.imeUpdate" -> {
                    decodeImeText(event)?.let { current = it }
                    emit(TextInputState.Focused(text = current))
                }

                "ms.remote.imeDone", "ms.remote.imeEnd" -> {
                    current = ""
                    emit(TextInputState.Idle)
                }
            }
        }
    }

    override val keyboardShowRequests: Flow<Unit> =
        registry.events
            .filter { it.optString("event") == "ms.remote.imeStart" }
            .map { }

    override suspend fun sendText(text: String): Result<Unit> = push(current + text)

    override suspend fun sendDelete(): Result<Unit> {
        if (current.isEmpty()) return Result.success(Unit)
        return push(current.dropLast(1))
    }

    override suspend fun replaceText(text: String): Result<Unit> = push(text)

    override suspend fun sendEnter(): Result<Unit> = registry.sendKey("KEY_ENTER")

    private fun push(newText: String): Result<Unit> =
        registry.sendText(newText).onSuccess { current = newText }

    private fun decodeImeText(event: JSONObject): String? = runCatching {
        val raw = event.opt("data")
        val encoded = when (raw) {
            is String -> raw
            is JSONObject -> raw.optString("text")
            else -> return null
        }
        String(Base64.decode(encoded, Base64.DEFAULT))
    }.getOrNull()
}