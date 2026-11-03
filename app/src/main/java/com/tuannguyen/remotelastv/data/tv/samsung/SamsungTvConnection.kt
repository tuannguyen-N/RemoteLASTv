package com.tuannguyen.remotelastv.data.tv.samsung

import android.util.Base64
import com.tuannguyen.remotelastv.domain.model.ConnectionStatus
import com.tuannguyen.remotelastv.domain.model.FailReason
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TvCredentials
import com.tuannguyen.remotelastv.domain.repository.TvConnectionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.net.InetAddress
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class SamsungTvConnection @Inject constructor(
    private val registry: SamsungSessionRegistry,
) : TvConnectionRepository {

    @Volatile private var userRequestedDisconnect = false

    override fun connect(
        television: Television,
        credentials: TvCredentials,
    ): Flow<ConnectionStatus> = callbackFlow {
        userRequestedDisconnect = false
        trySend(ConnectionStatus.Connecting)

        val savedToken = (credentials as? TvCredentials.Samsung)?.token?.takeIf { it.isNotBlank() }
        val request = Request.Builder().url(buildUrl(television.ipAddress, savedToken)).build()
        val client = buildClient(television.ipAddress)

        var connected = false

        val listener = object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (savedToken == null) trySend(ConnectionStatus.WaitingForAuth)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = runCatching { JSONObject(text) }.getOrNull() ?: return
                registry.onEvent(json)
                when (json.optString("event")) {
                    "ms.channel.connect" -> {
                        if (connected) return
                        connected = true
                        val token = json.optJSONObject("data")
                            ?.optString("token")
                            ?.takeIf { it.isNotBlank() }
                            ?: savedToken
                        registry.attach(television, webSocket)
                        trySend(
                            ConnectionStatus.Connected(
                                if (token != null) TvCredentials.Samsung(token) else TvCredentials.None
                            )
                        )
                    }

                    "ms.channel.unauthorized" -> fail(webSocket, FailReason.PAIRING_DECLINED)
                    "ms.channel.timeOut" -> fail(webSocket, FailReason.TIMEOUT)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                registry.detach(webSocket)
                if (connected) {
                    trySend(
                        ConnectionStatus.Disconnected(
                            recoverable = !userRequestedDisconnect,
                        )
                    )
                }
                channel.close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                registry.detach(webSocket)
                if (connected) {
                    trySend(
                        ConnectionStatus.Disconnected(
                            reason = t.message,
                            recoverable = true,
                        )
                    )
                } else {
                    trySend(ConnectionStatus.Failed(FailReason.UNKNOWN))
                }
                channel.close()
            }

            private fun fail(ws: WebSocket, reason: FailReason) {
                trySend(ConnectionStatus.Failed(reason))
                ws.close(1000, null)
            }
        }

        val socket = client.newWebSocket(request, listener)

        awaitClose {
            registry.detach(socket)
            socket.close(1000, null)
            client.dispatcher.executorService.shutdown()
        }
    }

    override suspend fun disconnect() {
        userRequestedDisconnect = true
        registry.close()
    }

    private fun buildUrl(ip: String, token: String?): String {
        val name = Base64.encodeToString(APP_NAME.toByteArray(), Base64.NO_WRAP)
        return buildString {
            append("wss://$ip:$PORT/api/v2/channels/samsung.remote.control?name=$name")
            if (token != null) append("&token=$token")
        }
    }

    private fun buildClient(ip: String): OkHttpClient {
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) =
                throw CertificateException("Client certs not supported")

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                if (chain.isEmpty()) throw CertificateException("Empty certificate chain")
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), SecureRandom())
        }
        val isLocalTarget = runCatching { InetAddress.getByName(ip).isSiteLocalAddress }
            .getOrDefault(false)

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { host, _ -> isLocalTarget && host == ip }
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(10, TimeUnit.SECONDS)
            .build()
    }

    private companion object {
        const val PORT = 8002
        const val APP_NAME = "RemoteLATV"
    }
}