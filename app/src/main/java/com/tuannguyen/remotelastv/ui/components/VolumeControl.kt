package com.tuannguyen.remotelastv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VolumeControl(
    volume: Int?,
    isMuted: Boolean,
    onVolumeUpClick: () -> Unit,
    onVolumeDownClick: () -> Unit,
    onMuteClick: () -> Unit,
) {
    val level = if (isMuted) 0 else volume
    val volumeIcon = when {
        level == 0 -> Icons.AutoMirrored.Rounded.VolumeOff
        (level ?: 100) < 50 -> Icons.AutoMirrored.Rounded.VolumeDown
        else -> Icons.AutoMirrored.Rounded.VolumeUp
    }

    Surface(
        shape = RoundedCornerShape(Dimens.Radius.controlSurface),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm),
        ) {
            HoldToRepeatButton(
                icon = Icons.Rounded.Remove,
                contentDescription = stringResource(R.string.cast_video_volume_down),
                onStep = onVolumeDownClick,
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMuteClick) {
                        Icon(
                            imageVector = volumeIcon,
                            contentDescription = stringResource(
                                if (isMuted) R.string.cast_video_unmute else R.string.cast_video_mute,
                            ),
                        )
                    }
                }
                if (level != null) {
                    LinearProgressIndicator(
                        progress = { level / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.Size.castVolumeProgressHeight)
                            .clip(CircleShape),
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                }
            }

            HoldToRepeatButton(
                icon = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.cast_video_volume_up),
                onStep = onVolumeUpClick,
            )
        }
    }
}


@Composable
private fun HoldToRepeatButton(
    icon: ImageVector,
    contentDescription: String,
    onStep: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnStep by rememberUpdatedState(onStep)
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .size(Dimens.Size.castVolumeButton)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .indication(interactionSource, ripple())
            .semantics(mergeDescendants = true) {
                role = Role.Button
                this.contentDescription = contentDescription
                onClick {
                    currentOnStep()
                    true
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        val repeatJob = scope.launch {
                            currentOnStep()
                            delay(HOLD_INITIAL_DELAY_MS)
                            while (true) {
                                currentOnStep()
                                delay(HOLD_REPEAT_INTERVAL_MS)
                            }
                        }
                        val released = tryAwaitRelease()
                        repeatJob.cancel()
                        interactionSource.emit(
                            if (released) PressInteraction.Release(press) else PressInteraction.Cancel(
                                press
                            ),
                        )
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private const val HOLD_INITIAL_DELAY_MS = 400L
private const val HOLD_REPEAT_INTERVAL_MS = 120L
