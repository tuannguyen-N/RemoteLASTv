package com.tuannguyen.remotelastv.ui.common

import androidx.annotation.DrawableRes
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.TvApp

@DrawableRes
fun TvApp.logoRes(): Int = when (this) {
    TvApp.NETFLIX -> R.drawable.img_logo_netflix
    TvApp.YOUTUBE -> R.drawable.img_logo_youtube
    TvApp.PRIME_VIDEO -> R.drawable.img_logo_prime_video
    TvApp.DISNEY_PLUS -> R.drawable.img_logo_disney
}