package com.tuannguyen.remotelastv.data.tv.samsung

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.repository.TvDiscoveryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.net.URL
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class SamsungTvDiscovery @Inject constructor(
    @ApplicationContext private val context: Context,
) : TvDiscoveryRepository {

    override val brand = TelevisionBrand.SAMSUNG

    override fun startScan(): Flow<Television> = callbackFlow {
        val appContext = context.applicationContext
        val wifiNetwork = findWifiNetwork(appContext)

        val multicastLock = (appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createMulticastLock("samsung-ssdp")
            .apply {
                setReferenceCounted(false)
                acquire()
            }

        val socket = DatagramSocket().apply {
            soTimeout = RECEIVE_TIMEOUT_MS
            wifiNetwork?.bindSocket(this)
        }

        launch(Dispatchers.IO) {
            val target = InetSocketAddress(InetAddress.getByName(SSDP_ADDRESS), SSDP_PORT)
            repeat(SEARCH_ROUNDS) {
                SEARCH_TARGETS.forEach { st ->
                    val data = buildSearchRequest(st).toByteArray()
                    try {
                        socket.send(DatagramPacket(data, data.size, target))
                    } catch (_: IOException) {
                    }
                }
                delay(SEARCH_INTERVAL_MS.milliseconds)
            }
        }

        launch(Dispatchers.IO) {
            val seen = mutableSetOf<String>()
            val buffer = ByteArray(2048)
            while (isActive) {
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    socket.receive(packet)
                } catch (_: SocketTimeoutException) {
                    continue
                } catch (_: IOException) {
                    break
                }

                val ip = packet.address?.hostAddress ?: continue
                if (packet.address !is Inet4Address || !seen.add(ip)) continue

                launch(Dispatchers.IO) {
                    fetchTelevision(ip, wifiNetwork)?.let { trySend(it) }
                }
            }
        }

        awaitClose {
            runCatching { socket.close() }
            if (multicastLock.isHeld) multicastLock.release()
        }
    }
        .flowOn(Dispatchers.IO)

    private fun fetchTelevision(ip: String, network: Network?): Television? {
        val url = URL("http://$ip:$REST_PORT/api/v2/")
        var conn: HttpURLConnection? = null
        return try {
            conn = (network?.openConnection(url) ?: url.openConnection()) as HttpURLConnection
            conn.connectTimeout = HTTP_TIMEOUT_MS
            conn.readTimeout = HTTP_TIMEOUT_MS
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return null

            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val device = json.optJSONObject("device") ?: return null

            val type = device.optString("type")
            if (type.isNotBlank() && !type.contains("Samsung", ignoreCase = true)) return null

            val id = device.optString("duid").ifBlank { device.optString("id") }
                .ifBlank { device.optString("wifiMac") }
                .ifBlank { ip }
            val name = device.optString("name").ifBlank { json.optString("name") }
                .ifBlank { device.optString("modelName") }
                .ifBlank { "Samsung TV" }

            Log.d(TAG, "found $name ip=$ip model=${device.optString("modelName")} id=$id")

            Television(
                id = id,
                name = name,
                ipAddress = ip,
                brand = TelevisionBrand.SAMSUNG,
            )
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    private fun findWifiNetwork(context: Context): Network? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.allNetworks.firstOrNull {
            cm.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }
    }

    private fun buildSearchRequest(st: String) =
        "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: $SSDP_ADDRESS:$SSDP_PORT\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 2\r\n" +
                "ST: $st\r\n\r\n"

    private companion object {
        const val TAG = "SamsungTvDiscovery"
        const val SSDP_ADDRESS = "239.255.255.250"
        const val SSDP_PORT = 1900
        const val REST_PORT = 8001
        const val SEARCH_ROUNDS = 3
        const val SEARCH_INTERVAL_MS = 1500L
        const val RECEIVE_TIMEOUT_MS = 1000
        const val HTTP_TIMEOUT_MS = 1500

        val SEARCH_TARGETS = listOf(
            "urn:samsung.com:device:RemoteControlReceiver:1",
            "urn:dial-multiscreen-org:service:dial:1",
            "urn:schemas-upnp-org:device:MediaRenderer:1",
        )
    }
}