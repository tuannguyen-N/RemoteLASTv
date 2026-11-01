package com.tuannguyen.remotelastv.domain.model

data class AudioTrack(
    val uri: String,
    val title: String,
    val durationMs: Long,
    val format: String,
    val mimeType: String?,
)