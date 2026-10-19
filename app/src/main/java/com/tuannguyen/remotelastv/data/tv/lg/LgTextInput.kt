package com.tuannguyen.remotelastv.data.tv.lg

import android.util.Log
import com.connectsdk.core.TextInputStatusInfo
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.service.capability.TextInputControl
import com.connectsdk.service.capability.TextInputControl.TextInputStatusListener
import com.connectsdk.service.command.ServiceCommandError
import com.connectsdk.service.command.ServiceSubscription
import com.tuannguyen.remotelastv.domain.model.TextCapability
import com.tuannguyen.remotelastv.domain.model.TextInputState
import com.tuannguyen.remotelastv.domain.repository.TvTextInputRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LgTextInput @Inject constructor(
    private val registry: LgDeviceRegistry,
) : TvTextInputRepository {

    override val capabilities: Set<TextCapability> = setOf(TextCapability.OBSERVE_FOCUS)

    override fun observeInput(): Flow<TextInputState> = callbackFlow {
        val control = textControl()
        if (control == null) {
            trySend(TextInputState.Unknown)
            close()
            return@callbackFlow
        }

        trySend(TextInputState.Idle)

        val subscription: ServiceSubscription<TextInputStatusListener> =
            control.subscribeTextInputStatus(object : TextInputStatusListener {
                override fun onSuccess(info: TextInputStatusInfo) {
                    Log.d("LgTextInput", "status focused=${info.isFocused} raw=${info.rawData}")
                    trySend(
                        if (info.isFocused) {
                            TextInputState.Focused(text = null, keyboardType = null)
                        } else {
                            TextInputState.Idle
                        }
                    )
                }

                override fun onError(error: ServiceCommandError) {
                    Log.e(
                        "LgTextInput",
                        "subscribe error code=${error.code} msg=${error.message}",
                        error
                    )
                    trySend(TextInputState.Unknown)
                }
            })

        awaitClose { runCatching { subscription.unsubscribe() } }
    }.distinctUntilChanged()

    override suspend fun sendText(text: String): Result<Unit> = runCatching {
        requireControl().sendText(text)
    }

    override suspend fun sendDelete(): Result<Unit> = runCatching {
        requireControl().sendDelete()
    }

    override suspend fun sendEnter(): Result<Unit> = runCatching {
        requireControl().sendEnter()
    }

    override suspend fun replaceText(text: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("LG không hỗ trợ thay thế toàn bộ text"))

    private fun textControl(): TextInputControl? {
        val device: ConnectableDevice = registry.activeDevice ?: return null
        return device.getCapability(TextInputControl::class.java)
    }

    private fun requireControl(): TextInputControl =
        textControl() ?: error("Chưa kết nối LG TV hoặc TV không hỗ trợ nhập text")
}