package com.tuannguyen.remotelastv.data.tv.cast.dlna

import android.content.Context
import android.net.wifi.WifiManager
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

data class SsdpResponse(val location: String, val usn: String?)

@Singleton
class SsdpDiscovery @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Gửi M-SEARCH và phát ra từng LOCATION (URL device description) nhận được.
     * unicastHost: gửi thêm M-SEARCH thẳng tới IP đó, hữu ích khi router chặn multicast.
     */
    fun search(timeoutMs: Long = 5_000, unicastHost: String? = null): Flow<SsdpResponse> = flow {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = wifi.createMulticastLock("remotelastv-ssdp").apply {
            setReferenceCounted(false)
            acquire()
        }
        val socket = MulticastSocket().apply {
            soTimeout = 500
            timeToLive = 4
        }
        try {
            val targets = buildList {
                add(InetSocketAddress(InetAddress.getByName(SSDP_ADDRESS), SSDP_PORT))
                if (unicastHost != null) {
                    add(InetSocketAddress(InetAddress.getByName(unicastHost), SSDP_PORT))
                }
            }

            fun sendAll() {
                for (st in SEARCH_TARGETS) {
                    val data = msearch(st).toByteArray(Charsets.UTF_8)
                    for (target in targets) {
                        runCatching { socket.send(DatagramPacket(data, data.size, target)) }
                    }
                }
            }

            sendAll()
            val start = SystemClock.elapsedRealtime()
            var resent = false
            val seen = HashSet<String>()
            val buffer = ByteArray(4096)

            while (SystemClock.elapsedRealtime() - start < timeoutMs) {
                currentCoroutineContext().ensureActive()
                // UDP có thể mất gói -> gửi lại 1 lần
                if (!resent && SystemClock.elapsedRealtime() - start > 1_500) {
                    sendAll()
                    resent = true
                }
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    socket.receive(packet)
                } catch (e: SocketTimeoutException) {
                    continue
                }
                val response = parse(String(packet.data, 0, packet.length, Charsets.UTF_8)) ?: continue
                if (seen.add(response.location)) emit(response)
            }
        } finally {
            socket.close()
            if (lock.isHeld) lock.release()
        }
    }.flowOn(Dispatchers.IO)

    private fun msearch(st: String) =
        "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: $SSDP_ADDRESS:$SSDP_PORT\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 2\r\n" +
                "ST: $st\r\n\r\n"

    private fun parse(raw: String): SsdpResponse? {
        if (!raw.startsWith("HTTP/")) return null
        val headers = HashMap<String, String>()
        raw.lineSequence().drop(1).forEach { line ->
            val i = line.indexOf(':')
            if (i > 0) headers[line.substring(0, i).trim().uppercase()] = line.substring(i + 1).trim()
        }
        val location = headers["LOCATION"] ?: return null
        return SsdpResponse(location, headers["USN"])
    }

    private companion object {
        const val SSDP_ADDRESS = "239.255.255.250"
        const val SSDP_PORT = 1900
        val SEARCH_TARGETS = listOf(
            "urn:schemas-upnp-org:device:MediaRenderer:1",
            "urn:schemas-upnp-org:service:AVTransport:1",
        )
    }
}