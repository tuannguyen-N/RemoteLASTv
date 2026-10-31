package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.repository.VideoThumbnailReader
import jakarta.inject.Inject

class GetVideoThumbnailUseCase @Inject constructor(
    private val reader: VideoThumbnailReader,
) {
    suspend operator fun invoke(uri: String): ByteArray? = reader.read(uri)
}
