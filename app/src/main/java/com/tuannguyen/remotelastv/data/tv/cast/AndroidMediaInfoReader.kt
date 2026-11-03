package com.tuannguyen.remotelastv.data.tv.cast

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.tuannguyen.remotelastv.domain.model.MediaInfo
import com.tuannguyen.remotelastv.domain.repository.MediaInfoReader
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

@Singleton
class AndroidMediaInfoReader @Inject constructor(
    @ApplicationContext private val context: Context,
) : MediaInfoReader {
    override suspend fun read(uri: String): MediaInfo = withContext(Dispatchers.IO) {
        val parsedUri = uri.toUri()
        val resolver = context.contentResolver
        val title = resolver.query(
            parsedUri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }?.takeIf(String::isNotBlank)
            ?: parsedUri.lastPathSegment.orEmpty()
        val mimeType = resolver.getType(parsedUri)
        logMediaDiagnostics(parsedUri, title, mimeType)
        MediaInfo(title, mimeType)
    }

    private fun logMediaDiagnostics(uri: Uri, title: String, mimeType: String?) {
        val sizeBytes = try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.SIZE),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) null else {
                    val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (index >= 0 && !cursor.isNull(index)) cursor.getLong(index)
                        .takeIf { it >= 0L } else null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read media size for diagnostics", e)
            null
        }
        val extension = title.substringAfterLast('.', "").lowercase().ifBlank { "unknown" }
        Log.i(
            TAG,
            "Media diagnostics: name=$title extension=$extension resolverMime=${mimeType ?: "unknown"} " +
                    "sizeBytes=${sizeBytes ?: "unknown"} uriScheme=${uri.scheme ?: "unknown"}",
        )

        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, emptyMap())
            Log.i(TAG, "Media track count=${extractor.trackCount}")
            for (index in 0 until extractor.trackCount) {
                Log.i(
                    TAG,
                    "Media track[$index]: ${extractor.getTrackFormat(index).toDiagnosticString()}"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not inspect media tracks; cast will continue", e)
        } finally {
            extractor.release()
        }
    }

    private fun MediaFormat.toDiagnosticString(): String = buildString {
        append("mime=${diagnosticString("mime") ?: "unknown"}")
        append(", durationUs=${diagnosticLong("durationUs") ?: "unknown"}")
        append(", width=${diagnosticInt("width") ?: "unknown"}")
        append(", height=${diagnosticInt("height") ?: "unknown"}")
        append(", frameRate=${diagnosticInt("frame-rate") ?: "unknown"}")
        append(", bitrate=${diagnosticInt("bitrate") ?: "unknown"}")
        append(", profile=${diagnosticInt("profile") ?: "unknown"}")
        append(", level=${diagnosticInt("level") ?: "unknown"}")
        append(", rotation=${diagnosticInt("rotation-degrees") ?: "unknown"}")
        append(", channels=${diagnosticInt("channel-count") ?: "unknown"}")
        append(", sampleRate=${diagnosticInt("sample-rate") ?: "unknown"}")
        append(", language=${diagnosticString("language") ?: "unknown"}")
    }

    private fun MediaFormat.diagnosticString(key: String): String? =
        try {
            if (containsKey(key)) getString(key) else null
        } catch (_: RuntimeException) {
            null
        }

    private fun MediaFormat.diagnosticInt(key: String): Int? =
        try {
            if (containsKey(key)) getInteger(key) else null
        } catch (_: RuntimeException) {
            null
        }

    private fun MediaFormat.diagnosticLong(key: String): Long? =
        try {
            if (containsKey(key)) getLong(key) else null
        } catch (_: RuntimeException) {
            null
        }

    private companion object {
        const val TAG = "AndroidMediaInfoReader"
    }
}
