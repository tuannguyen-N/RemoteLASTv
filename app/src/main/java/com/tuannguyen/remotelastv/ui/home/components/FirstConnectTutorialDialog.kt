package com.tuannguyen.remotelastv.ui.home.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.TutorialActions
import com.tuannguyen.remotelastv.ui.home.HomeUiState
import com.tuannguyen.remotelastv.ui.settings_connection_display.PreferenceRow
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import com.tuannguyen.remotelastv.ui.theme.RingColors

internal const val TUTORIAL_STEPS = 3

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
        ),
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = Dimens.screenPadding)
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(Modifier.padding(Dimens.Space.lg)) {
                TutorialHeader(step = step, onSkip = actions.onFinish)
                Spacer(Modifier.height(Dimens.Space.md))

                Column(
                    Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
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
                            1 -> ConnectionDisplayStep(uiState, actions)
                            else -> InputStep(uiState, actions)
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.screenPadding))
                TutorialFooter(
                    step = step,
                    onBack = actions.onBack,
                    onNext = actions.onNext,
                    onFinish = actions.onFinish,
                )
            }
        }
    }
}

@Composable
private fun BehaviorStepHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.xxs)) {
        Text(
            stringResource(R.string.tutorial_behavior_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            stringResource(R.string.tutorial_behavior_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConnectionDisplayStep(uiState: HomeUiState, actions: TutorialActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs)) {
        BehaviorStepHeader()
        GroupLabel(stringResource(R.string.tutorial_group_connection))
        PreferenceRow(
            title = stringResource(R.string.auto_reconnect),
            description = stringResource(R.string.auto_reconnect_description),
            checked = uiState.isAutoReconnectEnabled,
            onCheckedChange = actions.onAutoReconnectChanged,
        )
        PreferenceRow(
            title = stringResource(R.string.keep_screen_awake),
            description = stringResource(R.string.keep_screen_awake_description),
            checked = uiState.isKeepScreenAwakeEnabled,
            onCheckedChange = actions.onKeepScreenAwakeChanged,
        )
        PreferenceRow(
            title = stringResource(R.string.theater_mode),
            description = stringResource(R.string.theater_mode_description),
            checked = uiState.isTheaterModeEnabled,
            onCheckedChange = actions.onTheaterModeChanged,
        )
    }
}

@Composable
private fun InputStep(uiState: HomeUiState, actions: TutorialActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs)) {
        BehaviorStepHeader()
        GroupLabel(stringResource(R.string.tutorial_group_input))
        PreferenceRow(
            title = stringResource(R.string.haptic_feedback),
            description = stringResource(R.string.vibrate_when_buttons_are_pressed),
            checked = uiState.isHapticFeedbackEnabled,
            onCheckedChange = actions.onHapticFeedbackChanged,
        )
        PreferenceRow(
            title = stringResource(R.string.smooth_press),
            description = stringResource(R.string.repeat_commands_while_holding_buttons),
            checked = uiState.isSmoothPressEnabled,
            onCheckedChange = actions.onSmoothPressChanged,
        )
        PreferenceRow(
            title = stringResource(R.string.auto_keyboard),
            description = stringResource(R.string.automatically_show_the_keyboard_when_text_input_is_needed),
            checked = uiState.isAutoShowKeyboardEnabled,
            onCheckedChange = actions.onAutoShowKeyboardChanged,
        )
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = Dimens.Space.xs),
    )
}

@Composable
private fun LayoutStep(uiState: HomeUiState, actions: TutorialActions) {
    val mode = uiState.navigationMode
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.md)) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.xxs)) {
            Text(
                stringResource(R.string.tutorial_layout_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                stringResource(R.string.tutorial_change_later),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        RemotePreview(
            mode = mode,
            showMedia = uiState.showMediaButtons,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )

        Crossfade(targetState = mode, label = "mode_hint") { m ->
            Text(
                text = stringResource(
                    if (m == NavigationMode.D_PAD) R.string.tutorial_dpad_hint
                    else R.string.tutorial_touchpad_hint
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm)) {
            ModeTile(
                title = stringResource(R.string.settings_dpad),
                iconRes = R.drawable.ic_d_navigation,
                selected = mode == NavigationMode.D_PAD,
                onClick = { actions.onNavigationModeChanged(NavigationMode.D_PAD) },
                modifier = Modifier.weight(1f),
            )
            ModeTile(
                title = stringResource(R.string.settings_touchpad),
                iconRes = R.drawable.ic_tounch_navigation,
                selected = mode == NavigationMode.TOUCH_PAD,
                onClick = { actions.onNavigationModeChanged(NavigationMode.TOUCH_PAD) },
                modifier = Modifier.weight(1f),
            )
        }

        PreferenceRow(
            title = stringResource(R.string.media_controls),
            description = stringResource(R.string.show_or_hide_the_play_pause_next_and_previous_controls_on_your_remote),
            checked = uiState.showMediaButtons,
            onCheckedChange = actions.onMediaControlsChanged,
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainerLowest),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Crossfade(targetState = mode, label = "preview_mode") { m ->
            if (m == NavigationMode.D_PAD) DpadPreview() else TouchpadPreview()
        }
        AnimatedVisibility(
            visible = showMedia,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(40.dp, 52.dp, 40.dp).forEach { w ->
                    Box(
                        Modifier
                            .size(width = w, height = 28.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainerHighest)
                    )
                }
            }
        }
    }
}

@Composable
private fun DpadPreview() {
    Image(
        painter = painterResource(R.drawable.ic_d_navigation),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(112.dp),
    )
}

@Composable
private fun TouchpadPreview() {
    Image(
        painter = painterResource(R.drawable.ic_tounch_navigation),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(112.dp),
    )
}

@Composable
private fun ModeTile(
    title: String,
    @DrawableRes iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val borderColor by animateColorAsState(
        if (selected) colors.primary else colors.outlineVariant, label = "tile_border"
    )
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = if (selected) Dimens.Size.borderThin * 2 else Dimens.Size.borderThin,
                brush = if (selected) Brush.linearGradient(RingColors) else Brush.linearGradient(
                    listOf(borderColor, borderColor)
                ),
                shape = shape,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (selected) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
        Text(title, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun TutorialHeader(step: Int, onSkip: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(R.string.tutorial_step_of, step + 1, TUTORIAL_STEPS)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(TUTORIAL_STEPS) { i ->
                val width by animateDpAsState(if (i == step) 28.dp else 8.dp, label = "dot_w")
                Box(
                    Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(
                            if (i == step) Brush.linearGradient(RingColors)
                            else Brush.linearGradient(
                                listOf(colors.outlineVariant, colors.outlineVariant)
                            )
                        )
                )
            }
        }
        TextButton(
            onClick = onSkip,
            modifier = Modifier.height(40.dp)
        ) { Text(stringResource(R.string.tutorial_skip)) }
    }
}

@Composable
private fun TutorialFooter(
    step: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    val isLast = step == TUTORIAL_STEPS - 1
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm)) {
        if (step > 0) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                border = BorderStroke(
                    Dimens.Size.borderThin,
                    Brush.linearGradient(RingColors),
                ),
            ) { Text(stringResource(R.string.tutorial_back)) }
        }
        val buttonShape = RoundedCornerShape(50)
        Button(
            onClick = if (isLast) onFinish else onNext,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clip(buttonShape)
                .background(Brush.linearGradient(RingColors)),
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
            ),
        ) {
            Text(stringResource(if (isLast) R.string.tutorial_done else R.string.tutorial_next))
        }
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