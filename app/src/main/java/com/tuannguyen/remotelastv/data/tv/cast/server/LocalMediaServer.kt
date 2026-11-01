package com.tuannguyen.remotelastv.data.tv.cast.server

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import com.tuannguyen.remotelastv.data.tv.cast.dlna.DidlLite
import com.tuannguyen.remotelastv.domain.model.CastException
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import fi.iki.elonen.NanoHTTPD
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@Singleton
class LocalMediaServer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    data class PublishedMedia(val url: String, val sizeBytes: Long)

    private class Entry(val uri: Uri, val mime: String, val size: Long, val contentFeatures: String)

    private val entries = ConcurrentHashMap<String, Entry>()
    private val lifecycleLock = Any()

    private val executor = Executors.newFixedThreadPool(6) { r ->
        Thread(r, "cast-http").apply { isDaemon = true }
    }

    private var server: Server? = null
    private var wifiLock: WifiManager.WifiLock? = null

    /** Trả về URL http://<ip>:<port>/m/<token>.<ext> cho TV truy cập. */
    suspend fun publish(uri: Uri, mime: String, type: CastMediaType): PublishedMedia =
        withContext(Dispatchers.IO) {
            // Đọc metadata MỘT lần ở đây, không đọc lại ở mỗi request
            val size = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize }
                ?: throw CastException(CastFailReason.UNKNOWN)
            require(size > 0) { "Không xác định được kích thước file" }

            val token = UUID.randomUUID().toString().replace("-", "")
            entries[token] = Entry(uri, mime, size, DidlLite.contentFeatures(type))
            try {
                val port = ensureStarted()
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.let { ".$it" }
                    .orEmpty()
                PublishedMedia("http://${localIpv4()}:$port/m/$token$ext", size)
            } catch (e: Exception) {
                entries.remove(token)
                if (entries.isEmpty()) stop()
                throw e
            }
        }

    fun unpublish(url: String) {
        val token = url.substringAfterLast("/m/").substringBefore('.')
        entries.remove(token)
        if (entries.isEmpty()) stop()
    }

    fun stop() = synchronized(lifecycleLock) {
        entries.clear()
        server?.stop()
        server = null
        wifiLock?.takeIf { it.isHeld }?.release()
        wifiLock = null
    }

    // ---------------------------------------------------------------- server

    private fun ensureStarted(): Int = synchronized(lifecycleLock) {
        server?.let { return it.listeningPort }

        // Giữ Wi-Fi ở chế độ hiệu năng cao/độ trễ thấp trong lúc cast
        val wm = context.applicationContext.getSystemService(WifiManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= 29) WifiManager.WIFI_MODE_FULL_LOW_LATENCY
        else @Suppress("DEPRECATION") WifiManager.WIFI_MODE_FULL_HIGH_PERF
        wifiLock = wm.createWifiLock(mode, "remotelastv:cast").apply {
            setReferenceCounted(false); acquire()
        }

        Server().also {
            it.start(SOCKET_READ_TIMEOUT_MS, false)   // port 0 = hệ thống tự chọn cổng trống
            server = it
        }.listeningPort
    }

    private inner class Server : NanoHTTPD(0) {

        init {
            setAsyncRunner(PoolRunner())
        }

        override fun serve(session: IHTTPSession): Response {
            Log.d(
                "CastServer",
                "${session.method} ${session.uri} range=${session.headers["range"]} from=${session.headers["remote-addr"]}"
            )

            val method = session.method
            if (method != Method.GET && method != Method.HEAD) {
                return plain(Response.Status.METHOD_NOT_ALLOWED, "")
            }

            val token = session.uri.removePrefix("/m/").substringBefore('.')
            val entry = entries[token] ?: return plain(Response.Status.NOT_FOUND, "Not found")

            val range = parseRange(session.headers["range"], entry.size)
            if (range === INVALID_RANGE) {
                return plain(Response.Status.RANGE_NOT_SATISFIABLE, "").apply {
                    addHeader("Content-Range", "bytes */${entry.size}")
                }
            }

            val start = range?.first ?: 0L
            val end = range?.last ?: (entry.size - 1)
            val length = end - start + 1
            val status = if (range != null) Response.Status.PARTIAL_CONTENT else Response.Status.OK

            val response = if (method == Method.HEAD) {
                // Body rỗng; Content-Length đặt thủ công để TV biết kích thước
                newFixedLengthResponse(status, entry.mime, ByteArrayInputStream(ByteArray(0)), 0)
                    .apply { addHeader("Content-Length", length.toString()) }
            } else {
                val pfd = try {
                    context.contentResolver.openFileDescriptor(entry.uri, "r")
                } catch (e: Exception) {
                    null
                } ?: return plain(Response.Status.INTERNAL_ERROR, "")
                newFixedLengthResponse(
                    status,
                    entry.mime,
                    FileRangeInputStream(pfd, start, length),
                    length
                )
            }

            return response.apply {
                addHeader("Accept-Ranges", "bytes")
                if (range != null) addHeader("Content-Range", "bytes $start-$end/${entry.size}")
                addHeader(
                    "Connection",
                    "keep-alive"
                )          // TV tua video = nhiều Range request liên tiếp
                addHeader("Cache-Control", "no-store")
                // Một số TV (Samsung/DLNA) đòi các header này mới chịu phát/tua
                addHeader("transferMode.dlna.org", "Streaming")
                addHeader("contentFeatures.dlna.org", entry.contentFeatures)
            }
        }
    }

    private inner class PoolRunner : NanoHTTPD.AsyncRunner {
        private val running = Collections.synchronizedSet(mutableSetOf<NanoHTTPD.ClientHandler>())
        override fun closeAll() {
            synchronized(running) { running.toList() }.forEach { it.close() }
        }

        override fun closed(h: NanoHTTPD.ClientHandler) {
            running.remove(h)
        }

        override fun exec(h: NanoHTTPD.ClientHandler) {
            running.add(h); executor.execute(h)
        }
    }

    // ---------------------------------------------------------------- helpers

    private fun plain(status: NanoHTTPD.Response.Status, text: String) =
        NanoHTTPD.newFixedLengthResponse(status, "text/plain", text)

    private fun localIpv4(): String {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        cm.getLinkProperties(cm.activeNetwork)?.linkAddresses
            ?.map { it.address }
            ?.firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
            ?.let { return it.hostAddress!! }

        // Fallback (ví dụ phone đang phát hotspot, không có activeNetwork)
        return NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .first { it is Inet4Address && !it.isLoopbackAddress }
            .hostAddress!!
    }

    /** null = không có Range (trả full); INVALID_RANGE = 416; còn lại là khoảng byte hợp lệ. */
    private fun parseRange(header: String?, size: Long): LongRange? {
        if (header == null || !header.startsWith("bytes=")) return null
        val spec = header.removePrefix("bytes=").substringBefore(',')
            .trim()   // bỏ multi-range, lấy đoạn đầu
        val (a, b) = spec.split('-', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
        return try {
            val start: Long
            val end: Long
            if (a.isEmpty()) {                       // bytes=-500 : 500 byte cuối
                val n = b.toLong(); start = (size - n).coerceAtLeast(0); end = size - 1
            } else {
                start = a.toLong()
                end = if (b.isEmpty()) size - 1 else minOf(b.toLong(), size - 1)
            }
            if (start > end || start >= size) INVALID_RANGE else start..end
        } catch (e: NumberFormatException) {
            null
        }
    }

    companion object {
        private const val SOCKET_READ_TIMEOUT_MS = 60_000
        private val INVALID_RANGE = LongRange(1, 0)
    }
}

/**
 * Đọc đúng [length] byte bắt đầu từ [start] bằng FileChannel:
 * seek trực tiếp (O(1)) thay vì InputStream.skip(), không copy file ra cache.
 */
private class FileRangeInputStream(
    private val pfd: ParcelFileDescriptor,
    start: Long,
    private var remaining: Long,
) : InputStream() {

    private val input = FileInputStream(pfd.fileDescriptor)
    private val channel = input.channel.apply { position(start) }
    private val one = ByteArray(1)

    override fun read(): Int = if (read(one, 0, 1) == -1) -1 else one[0].toInt() and 0xFF

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (remaining <= 0) return -1
        val n = minOf(len.toLong(), remaining).toInt()
        val r = channel.read(ByteBuffer.wrap(b, off, n))
        if (r < 0) return -1
        remaining -= r
        return r
    }

    override fun available(): Int = minOf(remaining, Int.MAX_VALUE.toLong()).toInt()

    override fun close() {
        runCatching { input.close() }
        runCatching { pfd.close() }
    }
}