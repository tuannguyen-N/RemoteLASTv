package com.tuannguyen.remotelastv.data.tv.cast.dlna

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

data class UpnpService(val type: String, val controlUrl: String)

data class DlnaDevice(
    val udn: String,
    val friendlyName: String,
    val manufacturer: String?,
    val modelName: String?,
    val host: String,
    val avTransport: UpnpService,
    val renderingControl: UpnpService?,
) {
}

object UpnpDescription {

    /** Blocking: gọi trên Dispatchers.IO. */
    fun fetch(location: String): DlnaDevice? {
        val conn = URL(location).openConnection() as HttpURLConnection
        return try {
            conn.connectTimeout = 3_000
            conn.readTimeout = 3_000
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                null
            } else {
                parse(conn.inputStream.bufferedReader().use { it.readText() }, location)
            }
        } catch (e: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    /** Chỉ nhận thiết bị có dịch vụ AVTransport (kể cả khi nằm trong embedded device, như Sony). */
    fun parse(xml: String, location: String): DlnaDevice? = try {
        doParse(xml, location)
    } catch (e: Exception) {
        null
    }

    private fun doParse(xml: String, location: String): DlnaDevice? {
        val parser = Xml.newPullParser().apply { setInput(StringReader(xml)) }
        var udn: String? = null
        var name: String? = null
        var maker: String? = null
        var model: String? = null
        var urlBase: String? = null
        var inService = false
        var serviceType: String? = null
        var serviceControl: String? = null
        var avt: Pair<String, String>? = null
        var rc: Pair<String, String>? = null
        var tag = ""

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    tag = parser.name
                    if (tag == "service") {
                        inService = true
                        serviceType = null
                        serviceControl = null
                    }
                }

                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty()) {
                        if (inService) {
                            when (tag) {
                                "serviceType" -> serviceType = text
                                "controlURL" -> serviceControl = text
                            }
                        } else {
                            // Lấy giá trị đầu tiên = root device
                            when (tag) {
                                "UDN" -> if (udn == null) udn = text
                                "friendlyName" -> if (name == null) name = text
                                "manufacturer" -> if (maker == null) maker = text
                                "modelName" -> if (model == null) model = text
                                "URLBase" -> urlBase = text
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "service") {
                        inService = false
                        val type = serviceType
                        val control = serviceControl
                        if (type != null && control != null) {
                            if (avt == null && type.contains(":service:AVTransport:")) avt =
                                type to control
                            if (rc == null && type.contains(":service:RenderingControl:")) rc =
                                type to control
                        }
                    }
                    tag = ""
                }
            }
            event = parser.next()
        }

        val avtService = avt ?: return null
        val base = urlBase?.takeIf { it.isNotBlank() } ?: location
        fun resolve(path: String) = URL(URL(base), path).toString()
        val host = URL(location).host

        return DlnaDevice(
            udn = udn ?: location,
            friendlyName = name ?: host,
            manufacturer = maker,
            modelName = model,
            host = host,
            avTransport = UpnpService(avtService.first, resolve(avtService.second)),
            renderingControl = rc?.let { UpnpService(it.first, resolve(it.second)) },
        )
    }
}