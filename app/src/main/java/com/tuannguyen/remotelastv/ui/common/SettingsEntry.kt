package com.tuannguyen.remotelastv.ui.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.navigation.Route
import com.tuannguyen.remotelastv.ui.settings.SettingsAction

data class SettingsEntry(
    @StringRes val titleRes: Int,
    val icon: IconSource,
    val action: SettingsAction,
) {
    companion object {
        fun getRemoteEntries(): List<SettingsEntry> = listOf(
            SettingsEntry(
                R.string.settings_remote_layout,
                IconSource.Vector(Icons.Default.GridView),
                SettingsAction.Navigate(Route.RemoteLayout),
            ),
            SettingsEntry(
                R.string.remote_behavior,
                IconSource.Vector(Icons.Default.Tune),
                SettingsAction.Navigate(Route.RemoteBehavior),
            ),
            SettingsEntry(
                R.string.connection_display,
                IconSource.Vector(Icons.Default.Visibility),
                SettingsAction.Navigate(Route.ConnectionDisplay),
            ),
            SettingsEntry(
                R.string.settings_connected_devices,
                IconSource.Vector(Icons.Default.Tv),
                SettingsAction.Navigate(Route.ConnectedDevices),
            ),
        )

        fun getApplicationEntries(): List<SettingsEntry> = listOf(
            SettingsEntry(
                R.string.settings_language,
                IconSource.Vector(Icons.Default.Language),
                SettingsAction.Navigate(Route.Language),
            ),
            SettingsEntry(
                R.string.settings_faq,
                IconSource.Vector(Icons.AutoMirrored.Filled.HelpOutline),
                SettingsAction.Navigate(Route.Faq),
            ),
            SettingsEntry(
                R.string.settings_share,
                IconSource.Vector(Icons.Default.Share),
                SettingsAction.Share,
            ),
            SettingsEntry(
                R.string.settings_rate_review,
                IconSource.Vector(Icons.Default.Star),
                SettingsAction.RateAndReview,
            ),
            SettingsEntry(
                R.string.settings_feedback,
                IconSource.Vector(Icons.Default.Feedback),
                SettingsAction.Feedback,
            ),
        )
    }
}