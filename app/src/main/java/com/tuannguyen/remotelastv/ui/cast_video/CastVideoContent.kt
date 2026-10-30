package com.tuannguyen.remotelastv.ui.cast_video

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.common.bounceClickNoEffect
import com.tuannguyen.remotelastv.ui.common.gradientTint
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import java.util.Locale
import kotlin.math.roundToInt

data class CastVideoUiState(
    val title: String = "",
    val tvName: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Int = 0,
    val isMuted: Boolean = false,
)

@Composable
fun CastVideoContent(
    uiState: CastVideoUiState,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onMuteClick: () -> Unit,
    onStopCastingClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnail: @Composable BoxScope.() -> Unit = { ThumbnailPlaceholder() },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        CastingToHeader(
            tvName = uiState.tvName,
            onStopCastingClick = onStopCastingClick
        )

        Spacer(Modifier.height(24.dp))

        NowPlaying(title = uiState.title, thumbnail = thumbnail)

        Spacer(Modifier.weight(1f))

        ProgressSection(
            positionMs = uiState.positionMs,
            durationMs = uiState.durationMs,
        )

        Spacer(Modifier.height(28.dp))

        TransportControls(
            isPlaying = uiState.isPlaying,
            onRewindClick = onRewindClick,
            onPlayPauseClick = onPlayPauseClick,
            onForwardClick = onForwardClick,
        )

        Spacer(Modifier.weight(1f))

        VolumeControl(
            volume = uiState.volume,
            isMuted = uiState.isMuted,
            onVolumeChange = onVolumeChange,
            onMuteClick = onMuteClick,
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CastingToHeader(
    tvName: String,
    onStopCastingClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.Cast,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.cast_video_casting_to),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = tvName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.weight(1f))

        HeaderIcon(
            iconRes = R.drawable.ic_casting_disconnect,
            description = "Settings",
            onClick = onStopCastingClick,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun HeaderIcon(
    @DrawableRes iconRes: Int,
    description: String,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    tint: Color = LocalContentColor.current,
    tintBrush: Brush? = null,
) {
    Box(
        modifier = modifier
            .size(Dimens.Size.minTouchTarget)
            .bounceClickNoEffect(onClick = onClick)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        val iconModifier = Modifier
            .size(24.dp)
            .then(if (tintBrush != null) Modifier.gradientTint(tintBrush) else Modifier)
        if (imageVector != null) {
            Icon(
                imageVector = imageVector,
                contentDescription = description,
                tint = if (tintBrush != null) Color.Unspecified else tint,
                modifier = iconModifier,
            )
        } else {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = description,
                tint = if (tintBrush != null) Color.Unspecified else tint,
                modifier = iconModifier,
            )
        }
    }
}

@Composable
private fun NowPlaying(
    title: String,
    thumbnail: @Composable BoxScope.() -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            content = thumbnail,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BoxScope.ThumbnailPlaceholder() {
    Icon(
        imageVector = Icons.Rounded.Movie,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .align(Alignment.Center)
            .size(48.dp),
    )
}

@Composable
private fun ProgressSection(
    positionMs: Long,
    durationMs: Long,
) {
    val progress = if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    val timeStyle = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum")

    Column {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(positionMs),
                style = timeStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatTime(durationMs),
                style = timeStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TransportControls(
    isPlaying: Boolean,
    onRewindClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onForwardClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = onRewindClick,
            modifier = Modifier.size(72.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Replay10,
                contentDescription = stringResource(R.string.cast_video_rewind_10),
                modifier = Modifier.size(36.dp),
            )
        }

        FilledIconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier.size(96.dp),
        ) {
            Crossfade(targetState = isPlaying, label = "playPauseIcon") { playing ->
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(
                        if (playing) R.string.cast_video_pause else R.string.cast_video_play,
                    ),
                    modifier = Modifier.size(52.dp),
                )
            }
        }

        FilledTonalIconButton(
            onClick = onForwardClick,
            modifier = Modifier.size(72.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Forward10,
                contentDescription = stringResource(R.string.cast_video_forward_10),
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
private fun VolumeControl(
    volume: Int,
    isMuted: Boolean,
    onVolumeChange: (Int) -> Unit,
    onMuteClick: () -> Unit,
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    var lastSent by remember { mutableIntStateOf(-1) }

    val shown = if (isMuted) 0f else dragValue ?: volume.toFloat()
    val shownInt = shown.roundToInt()
    val volumeLabel = stringResource(R.string.cast_video_volume)

    val volumeIcon = when {
        shownInt == 0 -> Icons.Rounded.VolumeOff
        shownInt < 50 -> Icons.Rounded.VolumeDown
        else -> Icons.Rounded.VolumeUp
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMuteClick) {
                Icon(
                    imageVector = volumeIcon,
                    contentDescription = stringResource(
                        if (isMuted) R.string.cast_video_unmute else R.string.cast_video_mute,
                    ),
                )
            }
            Slider(
                value = shown,
                onValueChange = { value ->
                    dragValue = value
                    val rounded = value.roundToInt()
                    if (rounded != lastSent) {
                        lastSent = rounded
                        onVolumeChange(rounded)
                    }
                },
                onValueChangeFinished = {
                    dragValue = null
                    lastSent = -1
                },
                valueRange = 0f..100f,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = volumeLabel },
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = shownInt.toString(),
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                textAlign = TextAlign.End,
                modifier = Modifier.width(32.dp),
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 800)
@Composable
private fun CastVideoContentPlayingPreview() {
    RemoteLASTvTheme {
        Surface {
            CastVideoContent(
                uiState = CastVideoUiState(
                    title = "Family trip to Da Lat - day 2",
                    tvName = "Living room TV",
                    isPlaying = true,
                    positionMs = 754_000L,
                    durationMs = 3_725_000L,
                    volume = 35,
                ),
                onPlayPauseClick = {},
                onRewindClick = {},
                onForwardClick = {},
                onVolumeChange = {},
                onMuteClick = {},
                onStopCastingClick = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 800)
@Composable
private fun CastVideoContentPausedMutedPreview() {
    RemoteLASTvTheme {
        Surface {
            CastVideoContent(
                uiState = CastVideoUiState(
                    title = "A very long video title that should wrap onto two lines and then truncate gracefully",
                    tvName = "Samsung The Frame 55\" in the master bedroom",
                    isPlaying = false,
                    positionMs = 12_000L,
                    durationMs = 95_000L,
                    volume = 60,
                    isMuted = true,
                ),
                onPlayPauseClick = {},
                onRewindClick = {},
                onForwardClick = {},
                onVolumeChange = {},
                onMuteClick = {},
                onStopCastingClick = {},
            )
        }
    }
}