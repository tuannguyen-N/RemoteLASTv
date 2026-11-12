package com.tuannguyen.remotelastv.ui.settings_remote_layout.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.NavigationMode

data class NavigationOption(
    val mode: NavigationMode,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val imageRes: Int,
) {
    companion object {
        fun getNavigationOptions(): List<NavigationOption> = listOf(
            NavigationOption(
                mode = NavigationMode.D_PAD,
                titleRes = R.string.settings_dpad,
                descriptionRes = R.string.settings_dpad_description,
                imageRes = R.drawable.ic_d_navigation,
            ),
            NavigationOption(
                mode = NavigationMode.TOUCH_PAD,
                titleRes = R.string.settings_touchpad,
                descriptionRes = R.string.settings_touchpad_description,
                imageRes = R.drawable.ic_tounch_navigation,
            )
        )

        fun optionFor(mode: NavigationMode): NavigationOption =
            getNavigationOptions().firstOrNull { it.mode == mode } ?: getNavigationOptions().first()
    }
}