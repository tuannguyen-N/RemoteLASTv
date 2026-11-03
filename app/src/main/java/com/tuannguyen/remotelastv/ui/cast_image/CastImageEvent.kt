package com.tuannguyen.remotelastv.ui.cast_image

import com.tuannguyen.remotelastv.domain.model.CastFailReason

sealed interface CastImageEvent {
    data class CastFailed(val reason: CastFailReason) : CastImageEvent
    data class StopCompleted(val showInterstitial: Boolean) : CastImageEvent
}