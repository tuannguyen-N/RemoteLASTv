package com.tuannguyen.remotelastv.ui.cast_tv

import com.tuannguyen.remotelastv.domain.model.CastMediaType

sealed interface CastUiEvent {
    data class PickMedia(val type: CastMediaType) : CastUiEvent
}