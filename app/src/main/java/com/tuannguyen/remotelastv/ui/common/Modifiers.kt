package com.tuannguyen.remotelastv.ui.common

import android.os.SystemClock
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

fun Modifier.bounceClick(
    scaleDown: Float = 0.8f,
    shape: Shape = CircleShape,
    debounceMs: Long = 500L,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val safeClick = rememberDebouncedClick(debounceMs, onClick)

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = {
                haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress
                )
                safeClick()
            }
        )
}

fun Modifier.bounceClickNoEffect(
    scaleDown: Float = 0.80f,
    onClick: () -> Unit,
    debounceMs: Long = 500L,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val safeClick = rememberDebouncedClick(debounceMs, onClick)

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = safeClick
        )
}

fun Modifier.safeClickableNoEffect(
    onClick: () -> Unit,
    debounceMs: Long = 500L,
): Modifier = composed {
    val safeClick = rememberDebouncedClick(debounceMs, onClick)
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = safeClick
    )
}

fun Modifier.safeClickable(
    onClick: () -> Unit,
    debounceMs: Long = 500L
): Modifier = composed {
    val safeClick = rememberDebouncedClick(debounceMs, onClick)
    clickable(
        onClick = safeClick
    )
}

fun Modifier.clickableNoEffect(
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Composable
fun rememberDebouncedClick(
    intervalMs: Long = 500L,
    onClick: () -> Unit
): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val currentOnClick by rememberUpdatedState(onClick)
    return {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime >= intervalMs) {
            lastClickTime = now
            currentOnClick()
        }
    }
}