package com.tuannguyen.remotelastv.ui.common

import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.CastFailReason

fun CastFailReason.messageResource(): Int = when (this) {
    CastFailReason.DEVICE_NOT_FOUND -> R.string.cast_video_failure_device_not_found
    CastFailReason.CONNECT_TIMEOUT -> R.string.cast_video_failure_timeout
    CastFailReason.NOT_SUPPORTED -> R.string.cast_video_failure_not_supported
    CastFailReason.DISCONNECTED -> R.string.cast_video_failure_disconnected
    CastFailReason.UNKNOWN -> R.string.cast_video_failure_message
}