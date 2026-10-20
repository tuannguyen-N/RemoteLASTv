package com.tuannguyen.remotelastv.ui.settings_remote_layout.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import kotlinx.coroutines.launch

private enum class TouchpadGesture {
    SWIPE_ONE_FINGER,
    TAP,
    SWIPE_TWO_FINGERS
}

private data class TutorialPage(
    val gesture: TouchpadGesture,
    val titleRes: Int,
    val descriptionRes: Int,
    val noteRes: Int? = null
)

private val tutorialPages = listOf(
    TutorialPage(
        gesture = TouchpadGesture.SWIPE_ONE_FINGER,
        titleRes = R.string.touchpad_tutorial_swipe_title,
        descriptionRes = R.string.touchpad_tutorial_swipe_description,
        noteRes = R.string.touchpad_tutorial_swipe_note
    ),
    TutorialPage(
        gesture = TouchpadGesture.TAP,
        titleRes = R.string.touchpad_tutorial_tap_title,
        descriptionRes = R.string.touchpad_tutorial_tap_description
    ),
    TutorialPage(
        gesture = TouchpadGesture.SWIPE_TWO_FINGERS,
        titleRes = R.string.touchpad_tutorial_two_finger_title,
        descriptionRes = R.string.touchpad_tutorial_two_finger_description
    )
)

@Composable
fun TouchpadTutorialDialog(
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tutorialPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == tutorialPages.lastIndex

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.touchpad_tutorial_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.Space.md)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { index ->
                    TutorialPageContent(page = tutorialPages[index])
                }

                PagerIndicator(
                    pageCount = tutorialPages.size,
                    currentPage = pagerState.currentPage
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isLastPage) {
                        onDismiss()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(
                        if (isLastPage) R.string.touchpad_tutorial_got_it
                        else R.string.touchpad_tutorial_next
                    )
                )
            }
        },
        dismissButton = {
            if (!isLastPage) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.touchpad_tutorial_skip))
                }
            }
        }
    )
}

@Composable
private fun TutorialPageContent(page: TutorialPage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.Space.md)
    ) {
        GestureAnimation(
            gesture = page.gesture,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        page.noteRes?.let { noteRes ->
            Text(
                text = stringResource(noteRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateFloatAsState(
                targetValue = if (selected) 20f else 8f,
                animationSpec = tween(250),
                label = "indicatorWidth"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .size(width = width.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun GestureAnimation(
    gesture: TouchpadGesture,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "gesture")

    val loop by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loop"
    )

    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val padColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val fingerColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val padWidth = size.width * 0.7f
        val padHeight = size.height
        val padLeft = (size.width - padWidth) / 2f
        val corner = CornerRadius(24.dp.toPx())

        drawRoundRect(
            color = padColor,
            topLeft = Offset(padLeft, 0f),
            size = Size(padWidth, padHeight),
            cornerRadius = corner
        )
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(padLeft, 0f),
            size = Size(padWidth, padHeight),
            cornerRadius = corner,
            style = Stroke(width = 1.5.dp.toPx())
        )

        val centerX = padLeft + padWidth / 2f
        val centerY = padHeight / 2f

        when (gesture) {
            TouchpadGesture.SWIPE_ONE_FINGER -> {
                val angle = loop * 2f * Math.PI.toFloat()
                val x = centerX + padWidth * 0.28f * kotlin.math.sin(angle)
                val y = centerY + padHeight * 0.25f * kotlin.math.sin(angle * 2f)
                drawFinger(Offset(x, y), fingerColor)
            }

            TouchpadGesture.TAP -> {
                val center = Offset(centerX, centerY)
                drawCircle(
                    color = fingerColor.copy(alpha = (1f - pulse) * 0.5f),
                    radius = 14.dp.toPx() + 40.dp.toPx() * pulse,
                    center = center
                )
                val pressScale = if (pulse < 0.2f) 0.8f + pulse else 1f
                drawFinger(center, fingerColor, scale = pressScale)
            }

            TouchpadGesture.SWIPE_TWO_FINGERS -> {
                val directions = listOf(
                    Offset(1f, 0f),   // right
                    Offset(0f, 1f),   // down
                    Offset(-1f, 0f),  // left
                    Offset(0f, -1f)   // up
                )
                val scaled = loop * directions.size
                val dir = directions[scaled.toInt().coerceAtMost(directions.lastIndex)]
                val local = scaled - scaled.toInt()
                val eased = FastOutSlowInEasing.transform(local)

                val reach = if (dir.x != 0f) padWidth * 0.22f else padHeight * 0.22f
                val gap = 22.dp.toPx()
                val perp = Offset(-dir.y, dir.x)
                val center = Offset(centerX, centerY)

                listOf(-1f, 1f).forEach { side ->
                    val origin = center + perp * (gap * side)
                    val start = origin - dir * reach
                    val current = origin + dir * (reach * (eased * 2f - 1f))
                    drawArrowTrail(
                        from = start,
                        to = current,
                        color = fingerColor.copy(alpha = 0.3f)
                    )
                    drawFinger(current, fingerColor)
                }
            }
        }
    }
}

private fun DrawScope.drawFinger(
    center: Offset,
    color: Color,
    scale: Float = 1f
) {
    drawCircle(
        color = color.copy(alpha = 0.25f),
        radius = 22.dp.toPx() * scale,
        center = center
    )
    drawCircle(
        color = color,
        radius = 12.dp.toPx() * scale,
        center = center
    )
}

private fun DrawScope.drawArrowTrail(
    from: Offset,
    to: Offset,
    color: Color
) {
    drawLine(
        color = color,
        start = from,
        end = to,
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
}

@Preview(showBackground = true)
@Composable
private fun TouchpadTutorialDialogPreview() {
    RemoteLASTvTheme {
        TouchpadTutorialDialog(onDismiss = {})
    }
}