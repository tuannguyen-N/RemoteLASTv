package com.tuannguyen.remotelastv.data.tv.android_tv

import android.net.Uri
import android.util.Log
import com.tuannguyen.remotelastv.domain.model.VolumeState
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import remote.Remotemessage
import java.net.InetSocketAddress
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLSocket

/** Điều khiển TV sau khi đã ghép đôi (cổng 6466), dùng lại client cert trong AndroidKeyStore. */
@Singleton
class RemoteClient @Inject constructor(private val certManager: AndroidTvCertManager) {

    enum class Direction(val value: Int) { START_LONG(1), END_LONG(2), SHORT(3) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val writeLock = Any()
    private val _volumeState = MutableStateFlow<VolumeState?>(null)

    @Volatile private var socket: SSLSocket? = null
    @Volatile private var closedSignal = CompletableDeferred<Unit>().apply { complete(Unit) }

    val volumeState: Flow<VolumeState> = _volumeState.filterNotNull().distinctUntilChanged()

    val isConnected: Boolean
        get() = socket?.let { it.isConnected && !it.isClosed } == true

    /** Kết nối và chờ TV gửi RemoteStart. Trả về true khi đã sẵn sàng nhận phím. */
    suspend fun connect(ipAddress: String, alias: String, port: Int = PORT): Boolean =
        withContext(Dispatchers.IO) {
            disconnect()
            val ready = CompletableDeferred<Boolean>()
            val closed = CompletableDeferred<Unit>()
            closedSignal = closed
            try {
                val s = certManager.createSocketFactory(alias).createSocket() as SSLSocket
                socket = s
                s.connect(InetSocketAddress(ipAddress, port), CONNECT_TIMEOUT_MS)
                s.soTimeout = READ_TIMEOUT_MS // TV ping định kỳ, im lặng quá lâu = mất kết nối
                s.startHandshake()

                scope.launch { readLoop(s, ready, closed) }

                val ok = withTimeoutOrNull(READY_TIMEOUT_MS) { ready.await() } == true
                if (!ok) disconnect()
                ok
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "connect error", e)
                disconnect()
                false
            }
        }

    /** Suspend đến khi kết nối đóng (TV tắt, mất mạng, hoặc gọi disconnect()). */
    suspend fun awaitClosed() = closedSignal.await()

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: Exception) {
            Log.w(TAG, "disconnect error", e)
        } finally {
            socket = null
            _volumeState.value = null
            closedSignal.complete(Unit)
        }
    }

    /** keyCode = hằng số android.view.KeyEvent.KEYCODE_*. */
    suspend fun sendKey(keyCode: Int, direction: Direction = Direction.SHORT) =
        withContext(Dispatchers.IO) {
            val remoteKeyCode = requireNotNull(Remotemessage.RemoteKeyCode.forNumber(keyCode)) {
                "Unsupported Android key code: $keyCode"
            }
            val remoteDirection = when (direction) {
                Direction.START_LONG -> Remotemessage.RemoteDirection.START_LONG
                Direction.END_LONG -> Remotemessage.RemoteDirection.END_LONG
                Direction.SHORT -> Remotemessage.RemoteDirection.SHORT
            }
            send(
                Remotemessage.RemoteMessage.newBuilder().setRemoteKeyInject(
                    Remotemessage.RemoteKeyInject.newBuilder()
                        .setKeyCode(remoteKeyCode)
                        .setDirection(remoteDirection)
                ).build()
            )
        }

    suspend fun launchApp(appLink: String) =
        withContext(Dispatchers.IO) {
            require(appLink.isNotBlank()) { "App ID or link must not be blank" }
            val link = if (Uri.parse(appLink).scheme != null) {
                appLink
            } else {
                Uri.Builder()
                    .scheme("market")
                    .authority("launch")
                    .appendQueryParameter("id", appLink)
                    .build()
                    .toString()
            }
            send(
                Remotemessage.RemoteMessage.newBuilder().setRemoteAppLinkLaunchRequest(
                    Remotemessage.RemoteAppLinkLaunchRequest.newBuilder()
                        .setAppLink(link)
                ).build()
            )
        }

    private fun readLoop(
        s: SSLSocket,
        ready: CompletableDeferred<Boolean>,
        closed: CompletableDeferred<Unit>
    ) {
        try {
            val input = s.inputStream
            while (true) {
                val msg = Remotemessage.RemoteMessage.parseDelimitedFrom(input) ?: break
                when {
                    msg.hasRemoteConfigure() -> send(s, configureReply())
                    msg.hasRemoteSetActive() -> send(s,
                        Remotemessage.RemoteMessage.newBuilder().setRemoteSetActive(
                            Remotemessage.RemoteSetActive.newBuilder().setActive(FEATURES)
                        ).build()
                    )
                    msg.hasRemotePingRequest() -> send(s,
                        Remotemessage.RemoteMessage.newBuilder().setRemotePingResponse(
                            Remotemessage.RemotePingResponse.newBuilder()
                                .setVal1(msg.remotePingRequest.val1)
                        ).build()
                    )
                    msg.hasRemoteSetVolumeLevel() -> {
                        val volume = msg.remoteSetVolumeLevel
                        if (volume.volumeMax > 0) {
                            _volumeState.value = VolumeState(
                                level = (volume.volumeLevel.toLong() * 100 / volume.volumeMax)
                                    .toInt()
                                    .coerceIn(0, 100),
                                muted = volume.volumeMuted,
                            )
                        } else {
                            Log.w(TAG, "Ignoring volume update with max=${volume.volumeMax}")
                        }
                    }
                    msg.hasRemoteStart() -> ready.complete(true)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "readLoop ended: ${e.message}")
        } finally {
            ready.complete(false)
            try { s.close() } catch (_: Exception) {}
            if (socket === s) {
                socket = null
                _volumeState.value = null
            }
            closed.complete(Unit)
        }
    }

    private fun configureReply(): Remotemessage.RemoteMessage =
        Remotemessage.RemoteMessage.newBuilder().setRemoteConfigure(
            Remotemessage.RemoteConfigure.newBuilder()
                .setCode1(FEATURES)
                .setDeviceInfo(
                    Remotemessage.RemoteDeviceInfo.newBuilder()
                        .setModel("Google")
                        .setVendor("Google")
                        .setUnknown1(1)
                        .setUnknown2("1")
                        .setPackageName("androidtv-remote")
                        .setAppVersion("1.0.0")
                )
        ).build()

    private fun send(msg: Remotemessage.RemoteMessage) {
        val s = socket ?: throw IllegalStateException("Not connected")
        send(s, msg)
    }

    private fun send(s: SSLSocket, msg: Remotemessage.RemoteMessage) {
        synchronized(writeLock) {
            msg.writeDelimitedTo(s.outputStream)
            s.outputStream.flush()
        }
    }

    private companion object {
        const val TAG = "RemoteClient"
        const val PORT = 6466
        const val FEATURES = 622
        const val CONNECT_TIMEOUT_MS = 5_000
        const val READ_TIMEOUT_MS = 30_000
        const val READY_TIMEOUT_MS = 10_000L
    }
}