package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.CastMedia
import com.tuannguyen.remotelastv.domain.model.CastProviderId
import com.tuannguyen.remotelastv.domain.model.PreparedCastMedia

interface MediaPreparer {
    suspend fun prepare(media: CastMedia, providerId: CastProviderId): PreparedCastMedia
}
