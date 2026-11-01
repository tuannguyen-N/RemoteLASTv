package com.tuannguyen.remotelastv.ui.cast_audio

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.AudioTrack
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.domain.model.CastPlayerState
import com.tuannguyen.remotelastv.domain.model.CastRepeatMode
import com.tuannguyen.remotelastv.ui.cast_audio.components.MusicPickerDialog
import com.tuannguyen.remotelastv.ui.cast_video.ConnectionFailureOverlay
import com.tuannguyen.remotelastv.ui.common.messageResource
import com.tuannguyen.remotelastv.ui.components.CastingToHeader
import com.tuannguyen.remotelastv.ui.components.CustomSeekBar
import com.tuannguyen.remotelastv.ui.components.DialogLoading
import com.tuannguyen.remotelastv.ui.components.VolumeControl
import com.tuannguyen.remotelastv.ui.theme.ButtonColors
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import com.tuannguyen.remotelastv.ui.theme.extendedColors
import java.util.Locale

@Composable
fun CastAudioContent(
    uiState: CastAudioUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onVolumeUpClick: () -> Unit,
    onVolumeDownClick: () -> Unit,
    onMuteClick: () -> Unit,
    onStopCastingClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onTrackClick: (trackId: String) -> Unit,
    onMusicPickerClick: () -> Unit,
    onMusicPickerDismiss: () -> Unit,
    failureReason: CastFailReason?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CastingToHeader(
                tvName = uiState.tvName,
                onStopCastingClick = onStopCastingClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Dimens.Space.lg, vertical = Dimens.Space.md),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = Dimens.Space.lg, vertical = Dimens.Space.md),
            ) {
                PlayerCard(
                    uiState = uiState,
                    onPlayPauseClick = onPlayPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onRepeatClick = onRepeatClick,
                    onShuffleClick = onShuffleClick,
                    onSeekTo = onSeekTo,
                )

                Spacer(Modifier.height(Dimens.Space.md))

                PlaylistCard(
                    tracks = uiState.tracks,
                    currentTrackId = uiState.currentTrackUri,
                    onTrackClick = onTrackClick,
                    onMusicPickerClick = onMusicPickerClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )

                Spacer(Modifier.height(Dimens.Space.md))

                VolumeControl(
                    volume = uiState.volume,
                    isMuted = uiState.isMuted,
                    onVolumeUpClick = onVolumeUpClick,
                    onVolumeDownClick = onVolumeDownClick,
                    onMuteClick = onMuteClick,
                )
            }

            if (uiState.playerState == CastPlayerState.LOADING) {
                DialogLoading(
                    message = if (uiState.showLoadingMessage) {
                        stringResource(R.string.cast_audio_loading_message)
                    } else {
                        null
                    },
                    modal = false,
                )
            }

            failureReason?.let { reason ->
                ConnectionFailureOverlay(
                    title = stringResource(R.string.cast_audio_failure_title),
                    errorMessage = stringResource(reason.messageResource()),
                    retryLabel = stringResource(R.string.cast_audio_try_again),
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    if (uiState.isMusicPickerVisible) {
        MusicPickerDialog(
            tracks = uiState.tracks,
            selectedUris = setOfNotNull(uiState.currentTrackUri),
            isLoading = uiState.isLibraryLoading,
            onTrackClick = { track -> onTrackClick(track.uri) },
            onDismiss = onMusicPickerDismiss,
        )
    }
}

@Composable
private fun PlayerCard(
    uiState: CastAudioUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(Dimens.Radius.card),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Space.lg, vertical = Dimens.Space.md),
        ) {
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(Dimens.Space.sm))

            CustomSeekBar(
                positionMs = uiState.positionMs,
                durationMs = uiState.durationMs,
                onSeekTo = onSeekTo,
                enabled = uiState.currentTrackUri != null && uiState.durationMs > 0L,
            )

            Spacer(Modifier.height(Dimens.Space.sm))

            AudioTransportControls(
                isPlaying = uiState.playerState == CastPlayerState.PLAYING,
                canPlay = uiState.currentTrackUri != null,
                repeatMode = uiState.repeatMode,
                isShuffleEnabled = uiState.isShuffleEnabled,
                canSkip = uiState.currentTrackUri != null && uiState.tracks.size > 1,
                canRepeat = uiState.currentTrackUri != null,
                onPreviousClick = onPreviousClick,
                onRepeatClick = onRepeatClick,
                onPlayPauseClick = onPlayPauseClick,
                onShuffleClick = onShuffleClick,
                onNextClick = onNextClick,
            )
        }
    }
}

@Composable
private fun AudioTransportControls(
    isPlaying: Boolean,
    canPlay: Boolean,
    repeatMode: CastRepeatMode,
    isShuffleEnabled: Boolean,
    canSkip: Boolean,
    canRepeat: Boolean,
    onPreviousClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {


        ControlIconButton(
            imageVector = if (repeatMode == CastRepeatMode.ONE) {
                Icons.Rounded.RepeatOne
            } else {
                Icons.Rounded.Repeat
            },
            contentDescription = stringResource(
                when (repeatMode) {
                    CastRepeatMode.OFF -> R.string.cast_audio_repeat_off
                    CastRepeatMode.ALL -> R.string.cast_audio_repeat_all
                    CastRepeatMode.ONE -> R.string.cast_audio_repeat_one
                },
            ),
            onClick = onRepeatClick,
            enabled = canRepeat,
            active = repeatMode != CastRepeatMode.OFF,
        )

        ControlIconButton(
            imageVector = Icons.Rounded.SkipPrevious,
            contentDescription = stringResource(R.string.cast_audio_previous),
            onClick = onPreviousClick,
            enabled = canSkip,
        )

        FilledIconButton(
            onClick = onPlayPauseClick,
            enabled = canPlay,
            modifier = Modifier
                .size(PlayButtonSize)
                .background(Brush.linearGradient(ButtonColors), CircleShape),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            ),
        ) {
            Crossfade(targetState = isPlaying, label = "playPauseIcon") { playing ->
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(
                        if (playing) R.string.cast_audio_pause else R.string.cast_audio_play,
                    ),
                    modifier = Modifier.size(PlayIconSize),
                )
            }
        }

        ControlIconButton(
            imageVector = Icons.Rounded.SkipNext,
            contentDescription = stringResource(R.string.cast_audio_next),
            onClick = onNextClick,
            enabled = canSkip,
        )

        ControlIconButton(
            imageVector = Icons.Rounded.Shuffle,
            contentDescription = stringResource(R.string.cast_audio_shuffle),
            onClick = onShuffleClick,
            enabled = canSkip,
            active = isShuffleEnabled,
        )
    }
}

@Composable
private fun ControlIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(SideButtonSize)
            .alpha(if (enabled) 1f else 0.38f),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = if (active) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(Dimens.Size.iconMedium),
            )
            if (active) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun PlaylistCard(
    tracks: List<AudioTrack>,
    currentTrackId: String?,
    onTrackClick: (String) -> Unit,
    onMusicPickerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentTrackId, tracks.isNotEmpty()) {
        val index = tracks.indexOfFirst { it.uri == currentTrackId }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.Radius.card),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.cast_audio_list_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = Dimens.Space.lg),
                )

                Spacer(modifier.weight(1f))

                IconButton(
                    onClick = onMusicPickerClick,
                    modifier = Modifier.padding(end = Dimens.Space.xxs)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.Size.iconMedium),
                    )
                }
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = Dimens.Space.sm),
            ) {
                items(items = tracks, key = { it.uri }) { track ->
                    TrackRow(
                        track = track,
                        isCurrent = track.uri == currentTrackId,
                        onClick = { onTrackClick(track.uri) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: AudioTrack,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val highlight = if (isCurrent) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isCurrent, role = Role.Button, onClick = onClick)
            .background(highlight)
            .padding(horizontal = Dimens.Space.md, vertical = Dimens.Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrackThumbnail()

        Spacer(Modifier.width(Dimens.Space.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Dimens.Space.xxs))
            Text(
                text = "${formatTrackDuration(track.durationMs)} | ${track.format}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TrackThumbnail(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(TrackThumbnailSize)
            .clip(RoundedCornerShape(Dimens.Radius.card))
            .background(MaterialTheme.extendedColors.thumbnailBackground),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(35.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.extendedColors.thumbnailGradientStart,
                            MaterialTheme.extendedColors.thumbnailGradientEnd
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(Dimens.Size.iconSmall)
            )
        }
    }
}

private fun formatTrackDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}

private val PlayButtonSize = 60.dp
private val PlayIconSize = 30.dp
private val SideButtonSize = 48.dp
private val TrackThumbnailSize = 62.dp

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    heightDp = 800
)
@Composable
private fun CastAudioContentPreview() {
    val tracks = List(6) { index ->
        AudioTrack(
            uri = "track_$index",
            title = "Voice-rec-1234567890heheh_$index",
            durationMs = 22_000L,
            format = "mp3",
            mimeType = "audio/mpeg",
        )
    }
    RemoteLASTvTheme {
        Surface {
            CastAudioContent(
                uiState = CastAudioUiState(
                    title = "Voice-rec-123456789012553heheheh",
                    tvName = "Living room TV",
                    playerState = CastPlayerState.PLAYING,
                    positionMs = 12_000L,
                    durationMs = 72_000L,
                    volume = 35,
                    tracks = tracks,
                    currentTrackUri = "track_1",
                    repeatMode = CastRepeatMode.ALL,
                    isShuffleEnabled = true,
                ),
                onPlayPauseClick = {},
                onPreviousClick = {},
                onNextClick = {},
                onRepeatClick = {},
                onShuffleClick = {},
                onVolumeUpClick = {},
                onVolumeDownClick = {},
                onMuteClick = {},
                onStopCastingClick = {},
                onSeekTo = {},
                onTrackClick = {},
                onMusicPickerClick = {},
                onMusicPickerDismiss = {},
                failureReason = null,
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Music Picker - Dialog", showBackground = true)
@Composable
fun MusicPickerDialogPreview() {
    val titles =
        listOf("Đêm Trăng Tình Yêu", "Voice-rec-1234567890", "Nhạc không lời", "Morning Jazz")
    val tracks = titles.mapIndexed { index, title ->
        AudioTrack(
            uri = "content://demo/$index",
            title = title,
            durationMs = 22_000L + index * 61_000L,
            format = "mp3",
            mimeType = "audio/mpeg",
        )
    }
    RemoteLASTvTheme {
        MusicPickerDialog(
            tracks = tracks,
            selectedUris = setOf("content://demo/0"),
            onTrackClick = {},
            onDismiss = {},
        )
    }
}