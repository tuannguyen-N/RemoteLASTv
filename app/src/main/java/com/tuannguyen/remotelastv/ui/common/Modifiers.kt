package com.tuannguyen.remotelastv.ui.common

import android.os.SystemClock
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val LocalHapticEnabled = compositionLocalOf { true }
val LocalHoldToRepeatEnabled = compositionLocalOf { true }

fun Modifier.bounceClick(
    scaleDown: Float = 0.8f,
    shape: Shape = CircleShape,
    debounceMs: Long = 500L,
    hapticEnabled: Boolean? = null,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val globalHapticEnabled = LocalHapticEnabled.current
    val isHapticOn by rememberUpdatedState(hapticEnabled ?: globalHapticEnabled)

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
                if (isHapticOn) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
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

fun Modifier.repeatableClick(
    initialDelayMs: Long = 400L,
    repeatIntervalMs: Long = 100L,
    debounceMs: Long = 0L,
    holdEnabled: Boolean? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val hapticOn by rememberUpdatedState(LocalHapticEnabled.current)
    val holdOn by rememberUpdatedState(holdEnabled ?: LocalHoldToRepeatEnabled.current)
    val currentOnClick by rememberUpdatedState(onClick)
    val safeFirstClick = rememberDebouncedClick(debounceMs, onClick)
    val source = interactionSource ?: remember { MutableInteractionSource() }

    this
        .indication(source, indication)
        .semantics { this.onClick { currentOnClick(); true } }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    source.emit(press)

                    if (hapticOn) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    safeFirstClick()

                    val repeatJob = if (holdOn) {
                        coroutineScope.launch {
                            delay(initialDelayMs)
                            while (isActive) {
                                if (hapticOn) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                currentOnClick()
                                delay(repeatIntervalMs)
                            }
                        }
                    } else null

                    val released = tryAwaitRelease()
                    repeatJob?.cancel()
                    source.emit(
                        if (released) PressInteraction.Release(press)
                        else PressInteraction.Cancel(press)
                    )
                }
            )
        }
}

fun Modifier.bounceClickRepeatable(
    scaleDown: Float = 0.8f,
    shape: Shape = CircleShape,
    initialDelayMs: Long = 400L,
    repeatIntervalMs: Long = 100L,
    debounceMs: Long = 0L,
    holdEnabled: Boolean? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .repeatableClick(
            initialDelayMs = initialDelayMs,
            repeatIntervalMs = repeatIntervalMs,
            debounceMs = debounceMs,
            holdEnabled = holdEnabled,
            interactionSource = interactionSource,
            indication = ripple(),
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

@Composable
fun Modifier.attentionPulse(enabled: Boolean, color: Color): Modifier {
    if (!enabled) return this

    val transition = rememberInfiniteTransition(label = "tvSelectorAttention")

    val ring = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring",
    )
    val scale = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )

    return this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .drawBehind {
            val p = ring.value
            val grow = 14.dp.toPx() * p
            val w = size.width + grow * 2
            val h = size.height + grow * 2
            drawRoundRect(
                color = color.copy(alpha = (1f - p) * 0.5f),
                topLeft = Offset(-grow, -grow),
                size = Size(w, h),
                cornerRadius = CornerRadius(h / 2f),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
}