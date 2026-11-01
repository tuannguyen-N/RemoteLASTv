package com.tuannguyen.remotelastv.ui.cast_tv

import com.tuannguyen.remotelastv.domain.model.CastMediaType

data class CastTvUiState(
    val selectedMediaType: CastMediaType? = null,
    val connectedTvName: String? = null,
    val hasRequestedAudioPermission: Boolean = false,
) {
    val isConnected: Boolean get() = connectedTvName != null
}
