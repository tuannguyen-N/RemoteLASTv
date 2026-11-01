package com.tuannguyen.remotelastv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RingColors
import com.tuannguyen.remotelastv.ui.utils.formatTime

@Immutable
data class CustomSeekBarColors(
    val activeTrackColors: List<Color>,
    val thumbColor: Color,
    val inactiveTrackColor: Color,
    val timeColor: Color,
)

object CustomSeekBarDefaults {
    @Composable
    fun colors(
        activeColors: List<Color> = RingColors,
        thumbColor: Color = activeColors.first(),
        inactiveTrackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
        timeColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): CustomSeekBarColors = CustomSeekBarColors(
        activeTrackColors = activeColors,
        thumbColor = thumbColor,
        inactiveTrackColor = inactiveTrackColor,
        timeColor = timeColor,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = durationMs > 0L,
    showTime: Boolean = true,
    colors: CustomSeekBarColors = CustomSeekBarDefaults.colors(),
    trackHeight: Dp = Dimens.Size.seekBarTrackHeight,
    thumbSize: Dp = Dimens.Size.seekBarThumb,
) {
    val progress = if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }

    val isSeeking = remember { mutableStateOf(false) }
    val sliderProgress = remember { mutableFloatStateOf(progress) }
    LaunchedEffect(progress) {
        if (!isSeeking.value) sliderProgress.floatValue = progress
    }

    val displayedPositionMs = if (isSeeking.value && durationMs > 0L) {
        (sliderProgress.floatValue * durationMs).toLong().coerceIn(0L, durationMs)
    } else {
        positionMs
    }

    val activeBrush = remember(colors.activeTrackColors) {
        val list = colors.activeTrackColors
        if (list.size == 1) SolidColor(list.first()) else Brush.horizontalGradient(list)
    }
    val sliderColors = SliderDefaults.colors(
        thumbColor = colors.thumbColor,
        activeTrackColor = colors.activeTrackColors.first(),
        inactiveTrackColor = colors.inactiveTrackColor,
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier) {
        Slider(
            value = sliderProgress.floatValue,
            onValueChange = { value ->
                isSeeking.value = true
                sliderProgress.floatValue = value
            },
            onValueChangeFinished = {
                if (isSeeking.value) {
                    isSeeking.value = false
                    onSeekTo(
                        (sliderProgress.floatValue * durationMs).toLong().coerceIn(0L, durationMs)
                    )
                }
            },
            enabled = enabled,
            interactionSource = interactionSource,
            colors = sliderColors,
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = sliderColors,
                    enabled = enabled,
                    thumbSize = DpSize(thumbSize, thumbSize),
                )
            },
            track = { sliderState ->
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trackHeight),
                ) {
                    val trackSize = Size(size.width, size.height)
                    val cornerRadius = CornerRadius(size.height / 2f)

                    drawRoundRect(
                        color = colors.inactiveTrackColor,
                        size = trackSize,
                        cornerRadius = cornerRadius,
                    )
                    clipRect(right = size.width * sliderState.value.coerceIn(0f, 1f)) {
                        drawRoundRect(
                            brush = activeBrush,
                            size = trackSize,
                            cornerRadius = cornerRadius,
                        )
                    }
                }
            },
        )

        if (showTime) {
            Spacer(Modifier.height(Dimens.Space.xxs))
            val timeStyle = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatTime(displayedPositionMs),
                    style = timeStyle,
                    color = colors.timeColor,
                )
                Text(
                    text = formatTime(durationMs),
                    style = timeStyle,
                    color = colors.timeColor,
                )
            }
        }
    }
}