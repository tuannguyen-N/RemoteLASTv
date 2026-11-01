package com.tuannguyen.remotelastv.domain.model

data class CastMedia(
    val contentUri: String,
    val type: CastMediaType,
    val title: String,
    val mimeType: String? = null,
)