package com.tuannguyen.remotelastv.ui.cast_audio

import com.tuannguyen.remotelastv.domain.model.CastFailReason

sealed interface CastAudioEvent {
    data class StopCompleted(val showInterstitial: Boolean) : CastAudioEvent
    data class ShowMessage(val reason: CastFailReason) : CastAudioEvent
    data class CastFailed(val reason: CastFailReason) : CastAudioEvent
}