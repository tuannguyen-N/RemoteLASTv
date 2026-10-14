package com.tuannguyen.remotelastv.ui.settings_remote_layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.ui.components.CustomSwitch
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

private val NavigationPreviewSize = Dimens.Size.iconLarge * 3
private val NavigationImageSize = Dimens.Size.iconLarge * 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteLayoutContent(
    uiState: RemoteLayoutUiState,
    onNavigateBack: () -> Unit,
    onNavigationModeChanged: (NavigationMode) -> Unit,
    onMediaControlsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings_remote_layout))
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.settings_back
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding)
                .padding(bottom = Dimens.Space.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.md)
        ) {
            Spacer(Modifier.height(0.dp))

            NavigationOptionCard(
                title = stringResource(R.string.settings_dpad),
                description = stringResource(
                    R.string.settings_dpad_description
                ),
                iconRes = R.drawable.ic_d_navigation,
                selected = uiState.navigationMode == NavigationMode.D_PAD,
                onClick = {
                    onNavigationModeChanged(NavigationMode.D_PAD)
                }
            )

            NavigationOptionCard(
                title = stringResource(R.string.settings_touchpad),
                description = stringResource(
                    R.string.settings_touchpad_description
                ),
                iconRes = R.drawable.ic_tounch_navigation,
                selected = uiState.navigationMode == NavigationMode.TOUCH_PAD,
                onClick = {
                    onNavigationModeChanged(NavigationMode.TOUCH_PAD)
                }
            )

            PreferenceRow(
                title = stringResource(R.string.media_controls),
                description = stringResource(R.string.show_or_hide_the_play_pause_next_and_previous_controls_on_your_remote),
                checked = uiState.showMediaButtons,
                onCheckedChange = onMediaControlsChanged
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.Space.xs)
    )
}

@Composable
private fun NavigationOptionCard(
    title: String,
    description: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    SettingsOptionCard(
        selected = selected,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Dimens.Space.sm)
        ) {
            SelectionIndicator(selected = selected)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space.lg),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier.size(NavigationPreviewSize),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(NavigationImageSize)
                )
            }
        }
    }
}

@Composable
private fun PreferenceRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(Dimens.Space.md))

        CustomSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsOptionCard(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = Dimens.Size.borderThin,
                color = borderColor,
                shape = MaterialTheme.shapes.large
            )
            .clip(MaterialTheme.shapes.large)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Box {
            content()
        }
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean
) {
    RadioButton(
        selected = selected,
        onClick = null,
        modifier = Modifier.size(Dimens.Size.iconMedium)
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    RemoteLASTvTheme {
        RemoteLayoutContent(
            uiState = RemoteLayoutUiState(),
            onNavigateBack = {},
            onNavigationModeChanged = {},
            onMediaControlsChanged = {}
        )
    }
}