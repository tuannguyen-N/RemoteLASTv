package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.MediaInfo

interface MediaInfoReader {
    suspend fun read(uri: String): MediaInfo
}
