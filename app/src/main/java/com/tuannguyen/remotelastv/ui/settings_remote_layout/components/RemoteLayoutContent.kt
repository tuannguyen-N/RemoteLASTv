package com.tuannguyen.remotelastv.ui.settings_remote_layout.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.ui.components.CustomSwitch
import com.tuannguyen.remotelastv.ui.settings_remote_layout.RemoteLayoutUiState
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import com.tuannguyen.remotelastv.ui.theme.RingColors
private val StageImageSize = Dimens.Size.iconLarge * 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteLayoutContent(
    uiState: RemoteLayoutUiState,
    onNavigateBack: () -> Unit,
    onNavigationModeChanged: (NavigationMode) -> Unit,
    onMediaControlsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTouchpadTutorial by rememberSaveable { mutableStateOf(false) }

    if (showTouchpadTutorial) {
        TouchpadTutorialDialog(onDismiss = { showTouchpadTutorial = false })
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_remote_layout)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
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
                .padding(horizontal = Dimens.Space.screenHorizontal)
                .padding(bottom = Dimens.Space.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.lg)
        ) {
            RemotePreviewStage(
                navigationMode = uiState.navigationMode,
                showMediaButtons = uiState.showMediaButtons,
            )

            NavigationModeSelector(
                selected = uiState.navigationMode,
                onSelected = { mode ->
                    val isSwitchingToTouchpad =
                        mode == NavigationMode.TOUCH_PAD &&
                                uiState.navigationMode != NavigationMode.TOUCH_PAD
                    onNavigationModeChanged(mode)
                    if (isSwitchingToTouchpad) showTouchpadTutorial = true
                },
            )

            PreferenceRow(
                title = stringResource(R.string.media_controls),
                description = stringResource(
                    R.string.show_or_hide_the_play_pause_next_and_previous_controls_on_your_remote
                ),
                checked = uiState.showMediaButtons,
                onCheckedChange = onMediaControlsChanged
            )
        }
    }
}

@Composable
private fun RemotePreviewStage(
    navigationMode: NavigationMode,
    showMediaButtons: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Crossfade(
                targetState = NavigationOption.optionFor(navigationMode).imageRes,
                label = "navigationPreview",
            ) { imageRes ->
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(StageImageSize),
                )
            }

            Box(
                contentAlignment = Alignment.Center,
            ) {
                this@Column.AnimatedVisibility(
                    visible = showMediaButtons,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    MediaControlsPill(
                        modifier = Modifier.padding(top = Dimens.Space.sm)
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaControlsPill(
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = colors.surfaceContainerHighest,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimens.Space.lg,
                vertical = Dimens.Space.sm,
            ),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(Dimens.Size.iconMedium),
            )
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(Dimens.Size.iconMedium),
            )
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(Dimens.Size.iconMedium),
            )
        }
    }
}

@Composable
private fun NavigationModeSelector(
    selected: NavigationMode,
    onSelected: (NavigationMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationOptions = NavigationOption.getNavigationOptions()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.Space.md),
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            navigationOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option.mode == selected,
                    onClick = { onSelected(option.mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = navigationOptions.size,
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = RingColors[0],
                        activeContentColor = Color.White,
                    )
                ) {
                    Text(stringResource(option.titleRes))
                }
            }
        }

        Crossfade(
            targetState = NavigationOption.optionFor(selected),
            label = "navigationDescription",
        ) { option ->
            Text(
                text = stringResource(option.descriptionRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.Space.xs),
            )
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
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(horizontal = Dimens.Space.xs, vertical = Dimens.Space.sm)
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

@Preview(showBackground = true)
@Composable
private fun RemoteLayoutDpadPreview() {
    RemoteLASTvTheme {
        RemoteLayoutContent(
            uiState = RemoteLayoutUiState(),
            onNavigateBack = {},
            onNavigationModeChanged = {},
            onMediaControlsChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RemoteLayoutTouchpadNoMediaPreview() {
    RemoteLASTvTheme {
        RemoteLayoutContent(
            uiState = RemoteLayoutUiState(
                navigationMode = NavigationMode.TOUCH_PAD,
                showMediaButtons = false,
            ),
            onNavigateBack = {},
            onNavigationModeChanged = {},
            onMediaControlsChanged = {}
        )
    }
}