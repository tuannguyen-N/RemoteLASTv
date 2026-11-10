package com.tuannguyen.remotelastv.data.tv.samsung

import android.util.Base64
import com.tuannguyen.remotelastv.domain.model.Television
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.WebSocket
import org.json.JSONObject
import java.io.IOException

@Singleton
class SamsungSessionRegistry @Inject constructor() {

    @Volatile
    private var socket: WebSocket? = null
    @Volatile
    var activeTv: Television? = null
        private set

    private val _events = MutableSharedFlow<JSONObject>(extraBufferCapacity = 16)
    val events: SharedFlow<JSONObject> = _events.asSharedFlow()

    fun onEvent(json: JSONObject) {
        _events.tryEmit(json)
    }

    fun attach(tv: Television, ws: WebSocket) {
        socket = ws
        activeTv = tv
    }

    fun detach(ws: WebSocket) {
        if (socket === ws) {
            socket = null
            activeTv = null
        }
    }

    fun close() {
        socket?.close(1000, null)
    }

    fun sendKey(key: String): Result<Unit> = send(
        JSONObject()
            .put("method", "ms.remote.control")
            .put(
                "params", JSONObject()
                    .put("Cmd", "Click")
                    .put("DataOfCmd", key)
                    .put("Option", "false")
                    .put("TypeOfRemote", "SendRemoteKey")
            )
    )

    fun launchApp(appId: String, actionType: String = "DEEP_LINK"): Result<Unit> = send(
        JSONObject()
            .put("method", "ms.channel.emit")
            .put(
                "params", JSONObject()
                    .put("event", "ed.apps.launch")
                    .put("to", "host")
                    .put(
                        "data", JSONObject()
                            .put("action_type", actionType)
                            .put("appId", appId)
                    )
            )
    )


    fun sendText(text: String): Result<Unit> = send(
        JSONObject()
            .put("method", "ms.remote.control")
            .put(
                "params", JSONObject()
                    .put("Cmd", Base64.encodeToString(text.toByteArray(), Base64.NO_WRAP))
                    .put("DataOfCmd", "base64")
                    .put("TypeOfRemote", "SendInputString")
            )
    )

    fun moveCursor(dx: Int, dy: Int): Result<Unit> = send(
        JSONObject()
            .put("method", "ms.remote.control")
            .put(
                "params", JSONObject()
                    .put("Cmd", "Move")
                    .put(
                        "Position", JSONObject()
                            .put("x", dx)
                            .put("y", dy)
                            .put("Time", System.currentTimeMillis().toString())
                    )
                    .put("TypeOfRemote", "ProcessMouseDevice")
            )
    )

    fun mouseClick(): Result<Unit> = send(
        JSONObject()
            .put("method", "ms.remote.control")
            .put(
                "params", JSONObject()
                    .put("Cmd", "LeftClick")
                    .put("TypeOfRemote", "ProcessMouseDevice")
            )
    )

    private fun send(payload: JSONObject): Result<Unit> {
        val ws = socket
            ?: return Result.failure(IllegalStateException("Samsung TV not connected"))
        return if (ws.send(payload.toString())) Result.success(Unit)
        else Result.failure(IOException("Samsung send failed"))
    }
}