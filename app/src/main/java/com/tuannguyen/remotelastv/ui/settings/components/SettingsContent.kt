package com.tuannguyen.remotelastv.ui.settings.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.common.SettingsEntry
import com.tuannguyen.remotelastv.ui.common.bounceClick
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsHeader(
                titleRes = uiState.titleRes,
                onClose = { onAction(SettingsAction.Close) },
            )

            Column(
                modifier = Modifier.padding(horizontal = Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space.lg),
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_remote)) {
                    SettingsGroup(
                        entries = SettingsEntry.getRemoteEntries(),
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onAction = onAction,
                    )
                }

                SettingsSection(title = stringResource(R.string.settings_section_application)) {
                    SettingsGroup(
                        entries = SettingsEntry.getApplicationEntries(),
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onAction = onAction,
                    )
                }
            }

            Spacer(Modifier.height(Dimens.Space.lg))
        }
    }
}

@Composable
private fun SettingsHeader(
    @StringRes titleRes: Int,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenPadding, vertical = Dimens.Space.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Box(
            modifier = Modifier
                .size(Dimens.Size.minTouchTarget)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .bounceClick { onClose() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.a11y_close),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(Dimens.Size.iconMedium),
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.sm)) {
        Text(
            text = title,
            modifier = Modifier.padding(start = Dimens.Space.xs),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun SettingsGroup(
    entries: List<SettingsEntry>,
    iconContainerColor: Color,
    iconColor: Color,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(vertical = Dimens.Space.xs)
        ) {
            entries.forEachIndexed { index, entry ->
                SettingsItem(
                    title = stringResource(entry.titleRes),
                    icon = entry.icon,
                    onClick = { onAction(entry.action) },
                    showChevron = entry.action is SettingsAction.Navigate,
                    iconContainerColor = iconContainerColor,
                    iconColor = iconColor,
                )
                if (index != entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            start = Dimens.Space.md + SettingsIconTileSize + Dimens.Space.md,
                            end = Dimens.Space.md,
                        ),
                        thickness = Dimens.Size.borderThin,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    RemoteLASTvTheme {
        Surface {
            SettingsContent(uiState = SettingsUiState(), onAction = {})
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsContentDarkPreview() {
    RemoteLASTvTheme {
        Surface {
            SettingsContent(uiState = SettingsUiState(), onAction = {})
        }
    }
}