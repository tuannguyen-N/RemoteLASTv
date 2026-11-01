package com.tuannguyen.remotelastv.data.media

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.tuannguyen.remotelastv.domain.model.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioLibraryReader @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun readAll(): List<AudioTrack> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
        )
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC, ${MediaStore.Audio.Media._ID} DESC",
        ) ?: error("MediaStore returned no cursor for audio library")

        cursor.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val displayNameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            buildList {
                while (it.moveToNext()) {
                    val displayName = it.getString(displayNameColumn).orEmpty()
                    val mimeType = it.getString(mimeTypeColumn)
                    val extension = displayName
                        .substringAfterLast('.', missingDelimiterValue = "")
                        .lowercase()
                    val format = extension.takeIf(String::isNotBlank)
                        ?: mimeType
                            ?.let(MimeTypeMap.getSingleton()::getExtensionFromMimeType)
                            ?.lowercase()
                        ?: FALLBACK_FORMAT
                    val title = it.getString(titleColumn)
                        ?.takeIf(String::isNotBlank)
                        ?: displayName.substringBeforeLast('.', displayName)
                            .ifBlank { FALLBACK_TITLE }

                    add(
                        AudioTrack(
                            uri = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                it.getLong(idColumn),
                            ).toString(),
                            title = title,
                            durationMs = it.getLong(durationColumn).coerceAtLeast(0L),
                            format = format,
                            mimeType = mimeType,
                        ),
                    )
                }
            }
        }
    }

    private companion object {
        const val FALLBACK_FORMAT = "audio"
        const val FALLBACK_TITLE = "Audio"
    }
}
