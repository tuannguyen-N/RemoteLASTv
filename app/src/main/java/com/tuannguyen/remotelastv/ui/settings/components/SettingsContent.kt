package com.tuannguyen.remotelastv.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.common.IconSource
import com.tuannguyen.remotelastv.ui.common.bounceClick
import com.tuannguyen.remotelastv.ui.navigation.Route
import com.tuannguyen.remotelastv.ui.settings.SettingsAction
import com.tuannguyen.remotelastv.ui.settings.SettingsUiState
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.screenPadding,
                        vertical = Dimens.Space.lg,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "close",
                    modifier = Modifier
                        .size(Dimens.Size.iconLarge)
                        .bounceClick {
                            onAction(SettingsAction.Close)
                        },
                )
            }

            HorizontalDivider(
                thickness = Dimens.Size.borderThin,
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Remote section
            SettingsSectionTitle(
                title = stringResource(R.string.settings_section_remote),
                modifier = Modifier.padding(
                    start = Dimens.screenPadding,
                    end = Dimens.screenPadding,
                    top = Dimens.Space.md,
                    bottom = Dimens.Space.sm,
                ),
            )

            Column(
            ) {
                SettingsItem(
                    title = stringResource(R.string.settings_remote_layout),
                    onClick = { onAction(SettingsAction.Navigate(Route.RemoteLayout)) },
                )
                SettingsItem(
                    title = stringResource(R.string.remote_behavior),
                    onClick = { onAction(SettingsAction.Navigate(Route.RemoteBehavior)) },
                )

                SettingsItem(
                    title = stringResource(R.string.connection_display),
                    onClick = { onAction(SettingsAction.Navigate(Route.ConnectionDisplay)) },
                )

                SettingsItem(
                    title = stringResource(R.string.settings_connected_devices),
                    onClick = { onAction(SettingsAction.Navigate(Route.ConnectedDevices)) },
                )
            }

            Spacer(Modifier.height(Dimens.Space.xl))

            HorizontalDivider(
                thickness = Dimens.Size.borderThin,
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            SettingsSectionTitle(
                title = stringResource(R.string.settings_section_application),
                modifier = Modifier.padding(
                    start = Dimens.screenPadding,
                    end = Dimens.screenPadding,
                    top = Dimens.Space.md,
                    bottom = Dimens.Space.sm,
                ),
            )

            Column {
                SettingsItem(
                    title = stringResource(R.string.settings_faq),
                    onClick = { onAction(SettingsAction.Navigate(Route.Faq)) },
                )
                SettingsItem(
                    title = stringResource(R.string.settings_share),
                    onClick = { onAction(SettingsAction.Share) },
                    trailing = IconSource.Vector(Icons.Default.Share),
                )
                SettingsItem(
                    title = stringResource(R.string.settings_rate_review),
                    onClick = { onAction(SettingsAction.RateAndReview) },
                    trailing = IconSource.Vector(Icons.Default.Star),
                )
                SettingsItem(
                    title = stringResource(R.string.settings_feedback),
                    onClick = { onAction(SettingsAction.Feedback) },
                    trailing = IconSource.Vector(Icons.Default.Feedback),
                )
                SettingsItem(
                    title = stringResource(R.string.settings_legal),
                    onClick = { onAction(SettingsAction.Navigate(Route.Legal)) },
                )
                SettingsItem(
                    title = stringResource(R.string.settings_activate_premium),
                    onClick = { onAction(SettingsAction.ActivatePremium) },
                    trailing = IconSource.Vector(Icons.Default.LockOpen),
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    RemoteLASTvTheme {
        Surface {
            SettingsContent(
                uiState = SettingsUiState(),
                onAction = {},
            )
        }
    }
}