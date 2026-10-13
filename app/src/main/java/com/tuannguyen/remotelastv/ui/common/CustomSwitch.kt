package com.tuannguyen.remotelastv.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 56.dp,
    height: Dp = 30.dp,
    thumbPadding: Dp = 2.dp,
    checkedTrackColor: Color = Color(0xFF0FA59C),
    uncheckedTrackColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    thumbColor: Color = Color.White,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val thumbSize = height - thumbPadding * 2
    val travelPx = with(density) { (width - thumbSize - thumbPadding * 2).toPx() }

    val offset = remember { Animatable(if (checked) travelPx else 0f) }

    LaunchedEffect(checked, travelPx) {
        offset.animateTo(
            targetValue = if (checked) travelPx else 0f,
            animationSpec = tween(durationMillis = 200)
        )
    }

    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            offset.snapTo((offset.value + delta).coerceIn(0f, travelPx))
        }
    }

    val fraction = (offset.value / travelPx).coerceIn(0f, 1f)
    val trackColor = lerp(uncheckedTrackColor, checkedTrackColor, fraction)

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(CircleShape)
            .background(trackColor)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = onCheckedChange
            )
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                enabled = enabled,
                onDragStopped = {
                    val target = offset.value > travelPx / 2f
                    if (target != checked) {
                        onCheckedChange(target)
                    } else {
                        scope.launch {
                            offset.animateTo(
                                if (checked) travelPx else 0f,
                                tween(durationMillis = 200)
                            )
                        }
                    }
                }
            )
            .padding(thumbPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor)
                .border(width = 2.dp, color = trackColor, shape = CircleShape)
        )
    }
}

@Composable
private fun SettingRowPreview(
    title: String,
    initialChecked: Boolean,
    enabled: Boolean = true
) {
    var checked by remember { mutableStateOf(initialChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        CustomSwitch(
            checked = checked,
            onCheckedChange = { checked = it },
            enabled = enabled
        )
    }
}

@Preview(showBackground = true, name = "Settings list")
@Composable
private fun CustomSwitchPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            SettingRowPreview(title = "Dark Mode", initialChecked = true)
            SettingRowPreview(title = "Sound Effect", initialChecked = false)
            SettingRowPreview(title = "Notifications", initialChecked = true)
        }
    }
}

@Preview(showBackground = true, name = "Disabled")
@Composable
private fun CustomSwitchDisabledPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            SettingRowPreview(title = "Disabled (on)", initialChecked = true, enabled = false)
            SettingRowPreview(title = "Disabled (off)", initialChecked = false, enabled = false)
        }
    }
}
