package com.tuannguyen.remotelastv.ui.home.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.TutorialActions
import com.tuannguyen.remotelastv.ui.components.CustomSwitch
import com.tuannguyen.remotelastv.ui.home.HomeUiState
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import com.tuannguyen.remotelastv.ui.theme.RingColors

internal const val TUTORIAL_STEPS = 3
private val RingGradient = Brush.linearGradient(RingColors)

@Composable
fun FirstConnectTutorialDialog(
    step: Int,
    uiState: HomeUiState,
    actions: TutorialActions,
) {
    Dialog(
        onDismissRequest = { if (step > 0) actions.onBack() },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val dialogMaxHeight = this.maxHeight * 0.9f
            val scroll = rememberScrollState()
            LaunchedEffect(step) { scroll.scrollTo(0) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 560.dp)
                    .heightIn(max = dialogMaxHeight),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .safeDrawingPadding()
                        .padding(horizontal = Dimens.screenPadding),
                ) {
                    TutorialTopBar(step = step, onSkip = actions.onFinish)

                    Column(
                        modifier = Modifier
                            .heightIn(max = dialogMaxHeight * 0.68f)
                            .verticalScroll(scroll)
                            .padding(top = Dimens.Space.xs),
                    ) {
                        AnimatedContent(
                            targetState = step,
                            transitionSpec = {
                                val dir = if (targetState > initialState) 1 else -1
                                (slideInHorizontally { dir * it / 4 } + fadeIn()) togetherWith
                                        (slideOutHorizontally { -dir * it / 4 } + fadeOut())
                            },
                            label = "tutorial_step",
                        ) { s ->
                            when (s) {
                                0 -> LayoutStep(uiState, actions)
                                1 -> SettingsStep(
                                    title = R.string.tutorial_group_connection,
                                    items = connectionItems(uiState, actions),
                                )
                                else -> SettingsStep(
                                    title = R.string.tutorial_group_input,
                                    items = inputItems(uiState, actions),
                                )
                            }
                        }
                    }

                    TutorialBottomBar(
                        step = step,
                        onBack = actions.onBack,
                        onNext = actions.onNext,
                        onFinish = actions.onFinish,
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialTopBar(step: Int, onSkip: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val isLast = step == TUTORIAL_STEPS - 1
    val description = stringResource(R.string.tutorial_step_of, step + 1, TUTORIAL_STEPS)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space.xs),
        ) {
            repeat(TUTORIAL_STEPS) { i ->
                val color by animateColorAsState(
                    targetValue = if (i <= step) colors.primary else colors.outlineVariant,
                    label = "segment_color",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Spacer(Modifier.width(Dimens.Space.sm))

        TextButton(
            onClick = onSkip,
            enabled = !isLast,
            colors = ButtonDefaults.textButtonColors(contentColor = RingColors[1]),
            modifier = Modifier
                .alpha(if (isLast) 0f else 1f)
                .then(if (isLast) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
            Text(stringResource(R.string.tutorial_skip))
        }
    }
}

@Composable
private fun TutorialBottomBar(
    step: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    val isLast = step == TUTORIAL_STEPS - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Space.md),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (step > 0) {
            TextButton(
                onClick = onBack,
                colors = ButtonDefaults.textButtonColors(contentColor = RingColors[1]),
                modifier = Modifier.height(56.dp),
            ) {
                Text(stringResource(R.string.tutorial_back))
            }
        }

        Button(
            onClick = if (isLast) onFinish else onNext,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(RingGradient),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(if (isLast) R.string.tutorial_done else R.string.tutorial_next),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}


@Composable
private fun LayoutStep(uiState: HomeUiState, actions: TutorialActions) {
    val mode = uiState.navigationMode

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.lg)) {
        StepHeader(
            title = stringResource(R.string.tutorial_layout_title),
            subtitle = stringResource(R.string.tutorial_change_later),
        )

        RemotePreview(
            mode = mode,
            showMedia = uiState.showMediaButtons,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Crossfade(targetState = mode, label = "mode_hint") { m ->
            Text(
                text = stringResource(
                    if (m == NavigationMode.D_PAD) R.string.tutorial_dpad_hint
                    else R.string.tutorial_touchpad_hint
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        NavigationModePicker(
            mode = mode,
            onChange = actions.onNavigationModeChanged,
        )

        SettingGroup(
            items = listOf(
                SettingItem(
                    icon = Icons.Outlined.PlayCircle,
                    title = R.string.media_controls,
                    description = R.string.show_or_hide_the_play_pause_next_and_previous_controls_on_your_remote,
                    checked = uiState.showMediaButtons,
                    onCheckedChange = actions.onMediaControlsChanged,
                )
            )
        )
    }
}

@Composable
private fun RemotePreview(
    mode: NavigationMode,
    showMedia: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(40.dp)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Crossfade(targetState = mode, label = "preview_mode") { m ->
            Image(
                painter = painterResource(
                    if (m == NavigationMode.D_PAD) R.drawable.ic_d_navigation
                    else R.drawable.ic_tounch_navigation
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(112.dp),
            )
        }

        AnimatedVisibility(
            visible = showMedia,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Row(
                modifier = Modifier.padding(top = Dimens.Space.md),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm),
            ) {
                listOf(
                    Icons.Filled.SkipPrevious,
                    Icons.Filled.PlayArrow,
                    Icons.Filled.SkipNext,
                ).forEach { icon ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationModePicker(
    mode: NavigationMode,
    onChange: (NavigationMode) -> Unit,
) {
    val options = listOf(
        Triple(NavigationMode.D_PAD, R.string.settings_dpad, R.drawable.ic_d_navigation),
        Triple(NavigationMode.TOUCH_PAD, R.string.settings_touchpad, R.drawable.ic_tounch_navigation),
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label, iconRes) ->
            SegmentedButton(
                selected = mode == value,
                onClick = { onChange(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = RingColors[0],
                    activeContentColor = Color.White,
                ),
                icon = {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                    )
                },
                label = { Text(stringResource(label)) },
            )
        }
    }
}

@Composable
private fun SettingsStep(@StringRes title: Int, items: List<SettingItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.lg)) {
        StepHeader(
            title = stringResource(title),
            subtitle = stringResource(R.string.tutorial_behavior_subtitle),
        )
        SettingGroup(items)
    }
}

private fun connectionItems(ui: HomeUiState, a: TutorialActions) = listOf(
    SettingItem(
        Icons.Outlined.Autorenew,
        R.string.auto_reconnect, R.string.auto_reconnect_description,
        ui.isAutoReconnectEnabled, a.onAutoReconnectChanged,
    ),
    SettingItem(
        Icons.Outlined.LightMode,
        R.string.keep_screen_awake, R.string.keep_screen_awake_description,
        ui.isKeepScreenAwakeEnabled, a.onKeepScreenAwakeChanged,
    ),
    SettingItem(
        Icons.Outlined.Theaters,
        R.string.theater_mode, R.string.theater_mode_description,
        ui.isTheaterModeEnabled, a.onTheaterModeChanged,
    ),
)

private fun inputItems(ui: HomeUiState, a: TutorialActions) = listOf(
    SettingItem(
        Icons.Outlined.Vibration,
        R.string.haptic_feedback, R.string.vibrate_when_buttons_are_pressed,
        ui.isHapticFeedbackEnabled, a.onHapticFeedbackChanged,
    ),
    SettingItem(
        Icons.Outlined.TouchApp,
        R.string.smooth_press, R.string.repeat_commands_while_holding_buttons,
        ui.isSmoothPressEnabled, a.onSmoothPressChanged,
    ),
    SettingItem(
        Icons.Outlined.Keyboard,
        R.string.auto_keyboard, R.string.automatically_show_the_keyboard_when_text_input_is_needed,
        ui.isAutoShowKeyboardEnabled, a.onAutoShowKeyboardChanged,
    ),
)

private class SettingItem(
    val icon: ImageVector,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
)

@Composable
private fun SettingGroup(items: List<SettingItem>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.surfaceContainer),
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = Dimens.Space.md + 24.dp + Dimens.Space.md),
                    color = colors.outlineVariant,
                )
            }
            SettingRow(item)
        }
    }
}

@Composable
private fun SettingRow(item: SettingItem) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Cả dòng là vùng bấm; Switch chỉ hiển thị trạng thái
            .toggleable(
                value = item.checked,
                role = Role.Switch,
                onValueChange = item.onCheckedChange,
            )
            .padding(Dimens.Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = colors.onSurfaceVariant,
        )

        Spacer(Modifier.width(Dimens.Space.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.title),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
            )
            Text(
                text = stringResource(item.description),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(Dimens.Space.md))

        Box(modifier = Modifier.clearAndSetSemantics { }) {
            CustomSwitch(
                checked = item.checked,
                onCheckedChange = item.onCheckedChange,
            )
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, name = "Tutorial - Layout")
@Composable
private fun FirstConnectTutorialLayoutPreview() {
    RemoteLASTvTheme {
        FirstConnectTutorialDialog(
            step = 0,
            uiState = HomeUiState(
                navigationMode = NavigationMode.TOUCH_PAD,
                showMediaButtons = true,
            ),
            actions = previewTutorialActions(),
        )
    }
}

@Preview(showBackground = true, name = "Tutorial - Connection & display")
@Composable
private fun FirstConnectTutorialConnectionDisplayPreview() {
    RemoteLASTvTheme {
        FirstConnectTutorialDialog(
            step = 1,
            uiState = HomeUiState(
                isAutoReconnectEnabled = true,
                isKeepScreenAwakeEnabled = true,
                isTheaterModeEnabled = false,
                isHapticFeedbackEnabled = true,
                isSmoothPressEnabled = false,
                isAutoShowKeyboardEnabled = true,
            ),
            actions = previewTutorialActions(),
        )
    }
}

@Preview(showBackground = true, name = "Tutorial - Input")
@Composable
private fun FirstConnectTutorialInputPreview() {
    RemoteLASTvTheme {
        FirstConnectTutorialDialog(
            step = 2,
            uiState = HomeUiState(
                isHapticFeedbackEnabled = true,
                isSmoothPressEnabled = false,
                isAutoShowKeyboardEnabled = true,
            ),
            actions = previewTutorialActions(),
        )
    }
}

private fun previewTutorialActions() = TutorialActions(
    onNavigationModeChanged = {},
    onMediaControlsChanged = {},
    onAutoReconnectChanged = {},
    onKeepScreenAwakeChanged = {},
    onTheaterModeChanged = {},
    onHapticFeedbackChanged = {},
    onSmoothPressChanged = {},
    onAutoShowKeyboardChanged = {},
    onBack = {},
    onNext = {},
    onFinish = {},
)