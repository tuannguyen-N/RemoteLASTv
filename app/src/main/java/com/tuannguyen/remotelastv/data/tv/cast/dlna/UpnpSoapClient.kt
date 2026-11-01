package com.tuannguyen.remotelastv.data.tv.cast.dlna

import android.util.Xml
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

class UpnpException(val code: Int?, message: String) : Exception(message)

/** runCatching nhưng không nuốt CancellationException. */
inline fun <T> suspendRunCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}

object UpnpTime {
    fun format(ms: Long): String {
        val total = ms / 1000
        return "%d:%02d:%02d".format(Locale.US, total / 3600, (total % 3600) / 60, total % 60)
    }

    /** "H:MM:SS[.fff]" -> ms. "NOT_IMPLEMENTED" và các giá trị lạ -> null. */
    fun parse(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        val parts = value.trim().split(':')
        if (parts.size != 3) return null
        val h = parts[0].toLongOrNull() ?: return null
        val m = parts[1].toLongOrNull() ?: return null
        val s = parts[2].toDoubleOrNull() ?: return null
        return (h * 3600 + m * 60) * 1000 + (s * 1000).toLong()
    }
}

@Singleton
class UpnpSoapClient @Inject constructor() {

    // ---- AVTransport ----
    suspend fun setUri(d: DlnaDevice, uri: String, metadata: String) = call(
        d.avTransport, "SetAVTransportURI",
        "InstanceID" to "0", "CurrentURI" to uri, "CurrentURIMetaData" to metadata,
    )

    suspend fun play(d: DlnaDevice) =
        call(d.avTransport, "Play", "InstanceID" to "0", "Speed" to "1")

    suspend fun pause(d: DlnaDevice) = call(d.avTransport, "Pause", "InstanceID" to "0")
    suspend fun stop(d: DlnaDevice) = call(d.avTransport, "Stop", "InstanceID" to "0")

    suspend fun seek(d: DlnaDevice, positionMs: Long) {
        val target = UpnpTime.format(positionMs)
        try {
            call(
                d.avTransport,
                "Seek",
                "InstanceID" to "0",
                "Unit" to "REL_TIME",
                "Target" to target
            )
        } catch (e: UpnpException) {
            // Một số TV chỉ nhận ABS_TIME
            call(
                d.avTransport,
                "Seek",
                "InstanceID" to "0",
                "Unit" to "ABS_TIME",
                "Target" to target
            )
        }
    }

    suspend fun positionInfo(d: DlnaDevice) =
        call(d.avTransport, "GetPositionInfo", "InstanceID" to "0")

    suspend fun transportInfo(d: DlnaDevice) =
        call(d.avTransport, "GetTransportInfo", "InstanceID" to "0")

    // ---- RenderingControl ----
    suspend fun getVolume(d: DlnaDevice): Int? {
        val rc = d.renderingControl ?: return null
        return call(
            rc,
            "GetVolume",
            "InstanceID" to "0",
            "Channel" to "Master"
        )["CurrentVolume"]?.toIntOrNull()
    }

    suspend fun setVolume(d: DlnaDevice, percent: Int) {
        val rc = d.renderingControl ?: throw UpnpException(null, "TV không có RenderingControl")
        call(
            rc,
            "SetVolume",
            "InstanceID" to "0",
            "Channel" to "Master",
            "DesiredVolume" to percent.toString()
        )
    }

    suspend fun setMute(d: DlnaDevice, muted: Boolean) {
        val rc = d.renderingControl ?: throw UpnpException(null, "TV không có RenderingControl")
        call(
            rc,
            "SetMute",
            "InstanceID" to "0",
            "Channel" to "Master",
            "DesiredMute" to if (muted) "1" else "0"
        )
    }

    // ---- SOAP ----
    private suspend fun call(
        service: UpnpService,
        action: String,
        vararg args: Pair<String, String>,
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val body = envelope(service.type, action, args).toByteArray(Charsets.UTF_8)
        val conn = URL(service.controlUrl).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 3_000
            conn.readTimeout = 6_000
            conn.doOutput = true
            conn.setFixedLengthStreamingMode(body.size) // tránh chunked, nhiều TV không hiểu
            conn.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"")
            conn.setRequestProperty("SOAPACTION", "\"${service.type}#$action\"")
            conn.setRequestProperty("User-Agent", "Android/DLNADOC/1.50 UPnP/1.0 RemoteLATV/1.0")
            conn.outputStream.use { it.write(body) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val fields = parseFlat(text)
            if (code !in 200..299) {
                throw UpnpException(
                    fields["errorCode"]?.toIntOrNull(),
                    "$action thất bại: HTTP $code ${fields["errorDescription"].orEmpty()}".trim(),
                )
            }
            fields
        } finally {
            conn.disconnect()
        }
    }

    private fun envelope(
        serviceType: String,
        action: String,
        args: Array<out Pair<String, String>>
    ) =
        buildString {
            append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
            append("s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">")
            append("<s:Body><u:$action xmlns:u=\"$serviceType\">")
            for ((k, v) in args) append("<$k>").append(xmlEscape(v)).append("</$k>")
            append("</u:$action></s:Body></s:Envelope>")
        }

    /** Gom mọi phần tử lá thành map tên-local -> text. Đủ cho response của AVTransport/RenderingControl. */
    private fun parseFlat(xml: String): Map<String, String> {
        val map = HashMap<String, String>()
        if (xml.isBlank()) return map
        try {
            val parser = Xml.newPullParser().apply { setInput(StringReader(xml)) }
            var name: String? = null
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> name = parser.name.substringAfter(':')
                    XmlPullParser.TEXT -> {
                        val n = name
                        val t = parser.text.trim()
                        if (n != null && t.isNotEmpty()) map.putIfAbsent(n, t)
                    }

                    XmlPullParser.END_TAG -> name = null
                }
                event = parser.next()
            }
        } catch (e: Exception) {
            // XML lỗi (thường là response 500 của TV) -> trả phần đã đọc được
        }
        return map
    }
}