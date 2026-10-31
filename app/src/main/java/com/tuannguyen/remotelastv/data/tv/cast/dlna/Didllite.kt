package com.tuannguyen.remotelastv.data.tv.cast.dlna

import com.tuannguyen.remotelastv.domain.model.CastMediaType

internal fun xmlEscape(s: String): String = buildString(s.length + 16) {
    for (c in s) {
        when (c) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&apos;")
            else -> append(c)
        }
    }
}

object DidlLite {

    /** Dùng chung cho protocolInfo trong DIDL và header contentFeatures.dlna.org của HTTP server. */
    fun contentFeatures(type: CastMediaType): String = when (type) {
        CastMediaType.IMAGE ->
            "DLNA.ORG_OP=00;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=00f00000000000000000000000000000"
        else ->
            "DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01700000000000000000000000000000"
    }

    fun build(title: String, url: String, mimeType: String, type: CastMediaType, sizeBytes: Long): String {
        val upnpClass = when (type) {
            CastMediaType.VIDEO -> "object.item.videoItem"
            CastMediaType.AUDIO -> "object.item.audioItem.musicTrack"
            CastMediaType.IMAGE -> "object.item.imageItem.photo"
        }
        val protocolInfo = "http-get:*:$mimeType:${contentFeatures(type)}"
        return "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
                "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
                "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
                "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">" +
                "<item id=\"0\" parentID=\"-1\" restricted=\"1\">" +
                "<dc:title>${xmlEscape(title)}</dc:title>" +
                "<upnp:class>$upnpClass</upnp:class>" +
                "<res protocolInfo=\"${xmlEscape(protocolInfo)}\" size=\"$sizeBytes\">${xmlEscape(url)}</res>" +
                "</item></DIDL-Lite>"
    }
}