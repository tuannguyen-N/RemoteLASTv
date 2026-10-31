package com.tuannguyen.remotelastv.domain.repository

interface VideoThumbnailReader {
    suspend fun read(uri: String): ByteArray?
}
