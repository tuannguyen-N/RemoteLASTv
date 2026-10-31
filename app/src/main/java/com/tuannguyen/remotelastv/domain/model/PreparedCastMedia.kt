package com.tuannguyen.remotelastv.domain.model

interface PreparedCastMedia {
    val media: CastMedia

    suspend fun release()
}
