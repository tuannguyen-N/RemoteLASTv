package com.tuannguyen.remotelastv.ui.effects

import android.view.WindowManager
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.tuannguyen.remotelastv.ui.utils.findActivity
import kotlinx.coroutines.delay

/**
 * Adds [WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON] to the Activity window
 * while [enabled] is true. Automatically clears the flag when disabled or when
 * the composable leaves the composition.
 */
@Composable
fun KeepScreenOnEffect(enabled: Boolean) {
    val activity = LocalContext.current.findActivity()

    DisposableEffect(enabled, activity) {
        if (enabled && activity != null) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

/**
 * Theater Mode container — wraps [content] and manages screen dimming behaviour.
 *
 * When [enabled]:
 *  1. Keeps the screen on via [WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON].
 *  2. After [inactivityTimeoutMs] without any touch, dims the screen to the
 *     lowest brightness (`screenBrightness = 0f`) — identical to the system's
 *     dim-before-sleep effect.
 *  3. A transparent full-screen layer intercepts the **first tap** and restores
 *     the original system brightness, then resets the inactivity timer.
 *
 * When disabled (or when leaving the composition), all window modifications are
 * cleaned up.
 */
@Composable
fun TheaterModeContainer(
    enabled: Boolean,
    inactivityTimeoutMs: Long = 15_000L,
    content: @Composable () -> Unit,
) {
    val activity = LocalContext.current.findActivity()
    var isDimmed by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // ── Window flag management ───────────────────────────────────────────
    DisposableEffect(enabled, activity) {
        if (enabled && activity != null) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (activity != null) {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                activity.window.attributes = activity.window.attributes.apply {
                    screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
            }
            isDimmed = false
        }
    }

    // ── Inactivity timer → dim ──────────────────────────────────────────
    if (enabled && activity != null) {
        LaunchedEffect(lastInteractionTime) {
            delay(inactivityTimeoutMs)
            activity.window.attributes = activity.window.attributes.apply {
                screenBrightness = 0f
            }
            isDimmed = true
        }
    }

    // ── Layout ──────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {

        // Track every touch on the content (without consuming) to reset timer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled, isDimmed) {
                    if (enabled && !isDimmed) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial)
                                lastInteractionTime = System.currentTimeMillis()
                            }
                        }
                    }
                },
        ) {
            content()
        }

        // Transparent intercept layer — visible only while dimmed.
        // Catches the first tap → restores brightness & resets timer.
        if (enabled && isDimmed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            activity?.let { act ->
                                act.window.attributes = act.window.attributes.apply {
                                    screenBrightness =
                                        WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                                }
                            }
                            isDimmed = false
                            lastInteractionTime = System.currentTimeMillis()
                        }
                    },
            )
        }
    }
}
