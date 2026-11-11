package com.tuannguyen.remotelastv.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.PremiumPlan
import com.tuannguyen.remotelastv.domain.model.PremiumPlanId

@DrawableRes
fun KnownApp.logoRes(): Int = when (this) {
    KnownApp.NETFLIX -> R.drawable.img_logo_netflix
    KnownApp.YOUTUBE -> R.drawable.img_logo_youtube
    KnownApp.PRIME_VIDEO -> R.drawable.img_logo_prime_video
    KnownApp.DISNEY_PLUS -> R.drawable.img_logo_disney
    KnownApp.FPT_PLAY -> R.drawable.img_fpt_play
    KnownApp.APPLE_TV -> R.drawable.img_logo_apple_tv
    KnownApp.SPOTIFY -> R.drawable.img_logo_spotify
    KnownApp.PLEX -> R.drawable.img_plex_logo
    KnownApp.VTV_GO -> R.drawable.img_vtv_go
    KnownApp.GALAXY_PLAY -> R.drawable.img_galaxy_play
    KnownApp.HBO_MAX -> R.drawable.img_logo_hbo_max
    KnownApp.PARAMOUNT_PLUS -> R.drawable.img_logo_paramount_plus
    KnownApp.TIKTOK -> R.drawable.img_logo_tiktok
    KnownApp.YOUTUBE_KIDS -> R.drawable.img_logo_youtube_kids
}

@Composable
fun PremiumPlan.toUi(): PremiumPlanUi = when (id) {
    PremiumPlanId.YEAR -> PremiumPlanUi(
        id = id,
        title = stringResource(R.string.premium_plan_year),
        subtitle = stringResource(R.string.premium_plan_year_description),
        price = formattedPrice,
        badge = stringResource(R.string.premium_plan_most_popular),
    )

    PremiumPlanId.SIX_MONTHS -> PremiumPlanUi(
        id = id,
        title = stringResource(R.string.premium_plan_6_month),
        subtitle = stringResource(R.string.premium_plan_6_months_description),
        price = formattedPrice
    )

    PremiumPlanId.MONTH -> PremiumPlanUi(
        id = id,
        title = stringResource(R.string.premium_plan_month),
        subtitle = stringResource(R.string.premium_plan_month_description),
        price = formattedPrice,
    )

}