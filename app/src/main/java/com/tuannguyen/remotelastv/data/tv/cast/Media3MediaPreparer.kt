package com.tuannguyen.remotelastv.data.tv.cast

import android.net.Uri
import androidx.core.net.toUri
import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.domain.model.CastProviderId
import com.tuannguyen.remotelastv.domain.model.PreparedCastMedia
import com.tuannguyen.remotelastv.domain.repository.MediaPreparer
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Singleton
class Media3MediaPreparer @Inject constructor(
    private val videoTranscoder: HevcVideoTranscoder,
) : MediaPreparer {
    override suspend fun prepare(media: CastMedia, providerId: CastProviderId): PreparedCastMedia {
        val outputFile =
            if (providerId == CastProviderId.DLNA && media.type == CastMediaType.VIDEO) {
                videoTranscoder.transcodeIfNeeded(media.contentUri.toUri(), media.title)
            } else {
                null
            }
        val preparedMedia = if (outputFile == null) {
            media
        } else {
            media.copy(contentUri = Uri.fromFile(outputFile).toString(), mimeType = "video/mp4")
        }
        return PreparedMedia(preparedMedia, outputFile)
    }

    private class PreparedMedia(
        override val media: CastMedia,
        private val temporaryFile: File?,
    ) : PreparedCastMedia {
        override suspend fun release() {
            val file = temporaryFile ?: return
            withContext(Dispatchers.IO) {
                check(!file.exists() || file.delete()) {
                    "Could not delete temporary video ${file.name}"
                }
            }
        }
    }
}
