package com.tuannguyen.remotelastv.data.tv.cast

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class HevcVideoTranscoder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun transcodeIfNeeded(uri: Uri, displayName: String): File? {
        val videoTrack = withContext(Dispatchers.IO) { findVideoTrack(uri) } ?: return null
        val inputMime = videoTrack.stringOrNull(MediaFormat.KEY_MIME) ?: return null
        Log.i(
            TAG,
            "Source video: name=$displayName mime=$inputMime " +
                    "size=${videoTrack.longOrNull(MediaFormat.KEY_DURATION) ?: "unknown"}us " +
                    "resolution=${videoTrack.intOrNull(MediaFormat.KEY_WIDTH)}x${videoTrack.intOrNull(MediaFormat.KEY_HEIGHT)}",
        )
        if (inputMime !in HEVC_MIME_TYPES) return null
        val sourceWidth = videoTrack.intOrNull(MediaFormat.KEY_WIDTH) ?: 0
        val sourceHeight = videoTrack.intOrNull(MediaFormat.KEY_HEIGHT) ?: 0
        val rotation = videoTrack.intOrNull(MediaFormat.KEY_ROTATION) ?: 0
        val isPortrait = if (rotation == 90 || rotation == 270) {
            sourceWidth > sourceHeight
        } else {
            sourceHeight > sourceWidth
        }
        val outputHeight = if (isPortrait) PORTRAIT_HEIGHT_LIMIT else LANDSCAPE_HEIGHT_LIMIT
        val needsDownscale = maxOf(sourceWidth, sourceHeight) > LONG_EDGE_LIMIT
        Log.i(
            TAG,
            "Transcode profile: source=${sourceWidth}x$sourceHeight rotation=$rotation " +
                    "outputLongEdgeLimit=$LONG_EDGE_LIMIT downscale=$needsDownscale targetBitrate=$VIDEO_BITRATE",
        )

        val outputDirectory = File(context.cacheDir, OUTPUT_DIRECTORY)
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            error("Could not create temporary directory for video conversion")
        }
        val outputFile = File(outputDirectory, "${UUID.randomUUID()}.mp4")
        Log.i(TAG, "Transcoding HEVC to H.264 for DLNA: $displayName")

        try {
            transcode(uri, outputFile, outputHeight.takeIf { needsDownscale })
            check(outputFile.isFile && outputFile.length() > 0L) {
                "Video conversion completed without producing an output file"
            }
            Log.i(TAG, "Video conversion completed: bytes=${outputFile.length()}")
            return outputFile
        } catch (e: CancellationException) {
            outputFile.delete()
            throw e
        } catch (e: Exception) {
            outputFile.delete()
            Log.e(TAG, "HEVC to H.264 conversion failed for $displayName", e)
            throw IllegalStateException("Không thể chuyển video HEVC sang H.264 để phát trên TV", e)
        }
    }

    private fun findVideoTrack(uri: Uri): MediaFormat? {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(context, uri, emptyMap())
            (0 until extractor.trackCount)
                .asSequence()
                .map(extractor::getTrackFormat)
                .firstOrNull { it.stringOrNull(MediaFormat.KEY_MIME)?.startsWith("video/") == true }
        } catch (e: Exception) {
            Log.w(TAG, "Could not inspect video codec; using original media", e)
            null
        } finally {
            extractor.release()
        }
    }

    private suspend fun transcode(input: Uri, output: File, outputHeight: Int?) =
        suspendCancellableCoroutine<Unit> { continuation ->
            val encoderFactoryBuilder = DefaultEncoderFactory.Builder(context)
                .setEnableFallback(true)
            if (outputHeight != null) {
                encoderFactoryBuilder.setRequestedVideoEncoderSettings(
                    VideoEncoderSettings.Builder()
                        .setBitrate(VIDEO_BITRATE)
                        .build(),
                )
            }
            val transformer = try {
                Transformer.Builder(context)
                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                    .setEncoderFactory(encoderFactoryBuilder.build())
                    .experimentalSetMaxFramesInEncoder(MAX_FRAMES_IN_ENCODER)
                    .setUsePlatformDiagnostics(true)
                    .addListener(object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                            Log.i(
                                TAG,
                                "Transcode output: videoMime=${exportResult.videoMimeType} " +
                                        "videoEncoder=${exportResult.videoEncoderName} " +
                                        "resolution=${exportResult.width}x${exportResult.height} " +
                                        "videoBitrate=${exportResult.averageVideoBitrate} " +
                                        "audioMime=${exportResult.audioMimeType} " +
                                        "audioEncoder=${exportResult.audioEncoderName} " +
                                        "durationMs=${exportResult.durationMs} " +
                                        "bytes=${exportResult.fileSizeBytes}",
                            )
                            if (continuation.isActive) continuation.resume(Unit)
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException,
                        ) {
                            Log.e(
                                TAG,
                                "Transcode failed: code=${exportException.errorCodeName} " +
                                        "codec=${exportException.codecInfo} " +
                                        "videoEncoder=${exportResult.videoEncoderName} " +
                                        "audioEncoder=${exportResult.audioEncoderName}",
                                exportException,
                            )
                            if (continuation.isActive) continuation.resumeWithException(exportException)
                        }

                        override fun onFallbackApplied(
                            composition: Composition,
                            originalTransformationRequest: TransformationRequest,
                            fallbackTransformationRequest: TransformationRequest,
                        ) {
                            Log.w(
                                TAG,
                                "Media3 applied fallback: $originalTransformationRequest -> " +
                                        fallbackTransformationRequest,
                            )
                        }
                    })
                    .build()
            } catch (e: Exception) {
                continuation.resumeWithException(e)
                return@suspendCancellableCoroutine
            }

            continuation.invokeOnCancellation {
                transformer.cancel()
                output.delete()
            }

            try {
                val mediaItem = MediaItem.fromUri(input)
                val editedMediaItem = EditedMediaItem.Builder(mediaItem)
                    .setEffects(
                        Effects(
                            emptyList(),
                            outputHeight?.let { listOf(Presentation.createForHeight(it)) }.orEmpty(),
                        ),
                    )
                    .build()
                transformer.start(editedMediaItem, output.absolutePath)
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }
        }

    private fun MediaFormat.stringOrNull(key: String): String? =
        try {
            if (containsKey(key)) getString(key) else null
        } catch (_: RuntimeException) {
            null
        }

    private fun MediaFormat.intOrNull(key: String): Int? =
        try {
            if (containsKey(key)) getInteger(key) else null
        } catch (_: RuntimeException) {
            null
        }

    private fun MediaFormat.longOrNull(key: String): Long? =
        try {
            if (containsKey(key)) getLong(key) else null
        } catch (_: RuntimeException) {
            null
        }

    private companion object {
        const val TAG = "HevcVideoTranscoder"
        const val OUTPUT_DIRECTORY = "cast-transcoded"
        const val LANDSCAPE_HEIGHT_LIMIT = 720
        const val PORTRAIT_HEIGHT_LIMIT = 1_280
        const val LONG_EDGE_LIMIT = 1_280
        const val VIDEO_BITRATE = 2_500_000
        const val MAX_FRAMES_IN_ENCODER = 4
        val HEVC_MIME_TYPES = setOf("video/hevc", "video/h265", "video/x-h265")
    }
}
