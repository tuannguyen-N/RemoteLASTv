package com.tuannguyen.remotelastv.data.tv.roku

import android.content.Context
import android.net.wifi.WifiManager
import android.os.SystemClock
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.repository.TvDiscoveryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RokuDiscovery @Inject constructor(
    @ApplicationContext private val context: Context,
) : TvDiscoveryRepository {
    override val brand = TelevisionBrand.ROKU

    private val client = OkHttpClient.Builder()
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .build()

    private val searchMsg = ("M-SEARCH * HTTP/1.1\r\n" +
            "HOST: 239.255.255.250:1900\r\n" +
            "MAN: \"ssdp:discover\"\r\n" +
            "MX: 2\r\n" +
            "ST: roku:ecp\r\n\r\n").toByteArray()

    override fun startScan(): Flow<Television> = callbackFlow {
        val wifi = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = wifi.createMulticastLock("roku-ssdp").apply {
            setReferenceCounted(false)
            acquire()
        }
        val socket = DatagramSocket().apply { soTimeout = 1000 }

        val job = launch(Dispatchers.IO) {
            val target = DatagramPacket(
                searchMsg, searchMsg.size,
                InetAddress.getByName("239.255.255.250"), 1900
            )
            val seen = mutableSetOf<String>()
            val buf = ByteArray(2048)
            val end = SystemClock.elapsedRealtime() + 8_000
            var lastSend = 0L

            while (isActive && SystemClock.elapsedRealtime() < end) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastSend > 2_000) {
                    runCatching { socket.send(target) }
                    lastSend = now
                }
                val pkt = DatagramPacket(buf, buf.size)
                try {
                    socket.receive(pkt)
                } catch (_: SocketTimeoutException) {
                    continue
                }

                val text = String(pkt.data, 0, pkt.length)
                val location = text.lineSequence()
                    .firstOrNull { it.startsWith("location:", ignoreCase = true) }
                    ?.substringAfter(":")?.trim() ?: continue
                val ip = runCatching { URI(location).host }.getOrNull() ?: continue
                if (!seen.add(ip)) continue

                val info = fetchDeviceInfo(ip)
                val serial = info.tag("serial-number")
                trySend(
                    Television(
                        id = serial ?: ip,
                        name = info.tag("user-device-name")
                            ?: info.tag("friendly-device-name")
                            ?: "Roku $ip",
                        ipAddress = ip,
                        brand = TelevisionBrand.ROKU
                    )
                )
            }
        }

        awaitClose {
            job.cancel()
            socket.close()
            if (lock.isHeld) lock.release()
        }
    }
        .flowOn(Dispatchers.IO)

    private fun fetchDeviceInfo(ip: String): String = runCatching {
        client.newCall(Request.Builder().url("http://$ip:8060/query/device-info").build())
            .execute().use { it.body?.string().orEmpty() }
    }.getOrDefault("")

    private fun String.tag(name: String): String? =
        Regex("<$name>(.*?)</$name>").find(this)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
}