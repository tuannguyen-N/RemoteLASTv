package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.model.MediaInfo
import com.tuannguyen.remotelastv.domain.repository.MediaInfoReader
import jakarta.inject.Inject

class GetMediaInfoUseCase @Inject constructor(
    private val reader: MediaInfoReader,
) {
    suspend operator fun invoke(uri: String): MediaInfo = reader.read(uri)
}
