package com.tuannguyen.remotelastv.data.tv.android_tv

import android.view.KeyEvent
import com.tuannguyen.remotelastv.domain.model.TextCapability
import com.tuannguyen.remotelastv.domain.model.TextInputState
import com.tuannguyen.remotelastv.domain.repository.TvTextInputRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class AndroidTextInput @Inject constructor(
    private val remoteClient: RemoteClient,
) : TvTextInputRepository {

    override val capabilities: Set<TextCapability> =
        setOf(TextCapability.OBSERVE_FOCUS, TextCapability.REPLACE_TEXT, TextCapability.READ_TEXT)

    override val keyboardShowRequests: Flow<Unit> = remoteClient.imeShowRequests

    override fun observeInput(): Flow<TextInputState> =
        remoteClient.imeField
            .map { field ->
                if (field == null) {
                    TextInputState.Idle
                } else {
                    TextInputState.Focused(text = field.value, keyboardType = null)
                }
            }
            .distinctUntilChanged()

    override suspend fun sendText(text: String): Result<Unit> = runCatching {
        val field = requireField()
        val start = field.start.coerceIn(0, field.value.length)
        val end = field.end.coerceIn(start, field.value.length)
        val newValue = field.value.take(start) + text + field.value.substring(end)
        remoteClient.sendImeEdit(newValue, start + text.length)
    }

    override suspend fun replaceText(text: String): Result<Unit> = runCatching {
        requireField()
        remoteClient.sendImeEdit(text, text.length)
    }

    override suspend fun sendDelete(): Result<Unit> = runCatching {
        remoteClient.sendKey(KeyEvent.KEYCODE_DEL)
    }

    override suspend fun sendEnter(): Result<Unit> = runCatching {
        remoteClient.sendKey(KeyEvent.KEYCODE_ENTER)
    }

    private fun requireField(): RemoteClient.ImeField =
        checkNotNull(remoteClient.imeField.value) { "No active text field on TV" }
}