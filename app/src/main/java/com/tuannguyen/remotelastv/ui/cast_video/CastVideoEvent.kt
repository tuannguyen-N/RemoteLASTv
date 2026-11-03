package com.tuannguyen.remotelastv.ui.cast_video

import com.tuannguyen.remotelastv.domain.model.CastFailReason

sealed interface CastVideoEvent {
    data class StopCompleted(val showInterstitial: Boolean) : CastVideoEvent
    data class ShowMessage(val reason: CastFailReason) : CastVideoEvent
    data class CastFailed(val reason: CastFailReason) : CastVideoEvent
}