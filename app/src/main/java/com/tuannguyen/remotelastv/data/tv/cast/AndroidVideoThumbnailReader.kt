package com.tuannguyen.remotelastv.data.tv.cast

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.tuannguyen.remotelastv.domain.repository.VideoThumbnailReader
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@Singleton
class AndroidVideoThumbnailReader @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : VideoThumbnailReader {
    override suspend fun read(uri: String): ByteArray? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, Uri.parse(uri))
            val bitmap = retriever.getFrameAtTime(
                THUMBNAIL_TIME_US,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
            ) ?: retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            bitmap?.toJpeg()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract video thumbnail", e)
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.w(TAG, "Could not release video thumbnail retriever", e)
            }
        }
    }

    private fun Bitmap.toJpeg(): ByteArray {
        val output = ByteArrayOutputStream()
        return try {
            check(compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                "Could not encode video thumbnail"
            }
            output.toByteArray()
        } finally {
            recycle()
            output.close()
        }
    }

    private companion object {
        const val TAG = "AndroidVideoThumbnailReader"
        const val THUMBNAIL_TIME_US = 1_000_000L
        const val JPEG_QUALITY = 85
    }
}
