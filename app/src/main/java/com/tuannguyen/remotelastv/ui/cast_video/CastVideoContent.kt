package com.tuannguyen.remotelastv.ui.cast_video

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChangeCircle
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.domain.model.CastPlayerState
import com.tuannguyen.remotelastv.ui.common.messageResource
import com.tuannguyen.remotelastv.ui.components.CastingToHeader
import com.tuannguyen.remotelastv.ui.components.CustomSeekBar
import com.tuannguyen.remotelastv.ui.components.DialogLoading
import com.tuannguyen.remotelastv.ui.components.VolumeControl
import com.tuannguyen.remotelastv.ui.theme.ButtonColors
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CastVideoContent(
    uiState: CastVideoUiState,
    onPlayPauseClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onVolumeUpClick: () -> Unit,
    onVolumeDownClick: () -> Unit,
    onMuteClick: () -> Unit,
    onStopCastingClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onChangeVideo: () -> Unit,
    failureReason: CastFailReason?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnail: @Composable BoxScope.() -> Unit = { ThumbnailPlaceholder() },
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
                    .padding(horizontal = Dimens.Space.lg, vertical = Dimens.Space.md),
            ) {
                val thumbnailBytes = uiState.thumbnailBytes
                NowPlaying(
                    title = uiState.title,
                    onChangeVideo = onChangeVideo,
                    thumbnail = if (thumbnailBytes == null) {
                        thumbnail
                    } else {
                        { VideoThumbnail(thumbnailBytes) }
                    },
                )

                Spacer(Modifier.weight(1f))

                CustomSeekBar(
                    positionMs = uiState.positionMs,
                    durationMs = uiState.durationMs,
                    onSeekTo = onSeekTo,
                )

                Spacer(Modifier.height(Dimens.Size.castControls))

                TransportControls(
                    isPlaying = uiState.playerState == CastPlayerState.PLAYING,
                    onRewindClick = onRewindClick,
                    onPlayPauseClick = onPlayPauseClick,
                    onForwardClick = onForwardClick,
                )

                Spacer(Modifier.weight(1f))

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
                        stringResource(R.string.cast_video_loading_message)
                    } else {
                        null
                    },
                    modal = false,
                )
            }

            failureReason?.let { reason ->
                ConnectionFailureOverlay(
                    title = stringResource(R.string.cast_video_failure_title),
                    errorMessage = stringResource(reason.messageResource()),
                    retryLabel = stringResource(R.string.cast_video_try_again),
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}


@Composable
fun ConnectionFailureOverlay(
    modifier: Modifier = Modifier,
    title: String = "Connection Failed",
    errorMessage: String = "Unable to connect to your TV. Please try again.",
    retryLabel: String = "Try Again",
    onRetry: () -> Unit = {},
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.error)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Space.lg),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(Dimens.Space.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LottieAnimation(
                    composition = composition,
                    modifier = Modifier.size(Dimens.Size.dialogIllustration),
                )

                Spacer(Modifier.height(Dimens.Space.xs))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(Dimens.Space.xxs))

                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(Dimens.Space.md))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.Space.xs),
                ) {
                    Text(text = retryLabel)
                }
            }
        }
    }
}

@Composable
private fun NowPlaying(
    title: String,
    onChangeVideo: () -> Unit,
    thumbnail: @Composable BoxScope.() -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(Dimens.Radius.card))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            thumbnail()

            FilledTonalIconButton(
                onClick = onChangeVideo,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.Space.xs),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChangeCircle,
                    contentDescription = "Change video",
                )
            }
        }

        Spacer(Modifier.height(Dimens.Space.md))

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
            .size(Dimens.Size.castThumbnailPlaceholder),
    )
}

@Composable
private fun BoxScope.VideoThumbnail(bytes: ByteArray) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, bytes) {
        value = withContext(Dispatchers.IO) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }

    val thumbnail = bitmap
    thumbnail?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    } ?: ThumbnailPlaceholder()
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
        horizontalArrangement = Arrangement.spacedBy(
            Dimens.Size.castControls,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = onRewindClick,
            modifier = Modifier.size(Dimens.Size.castSeekButton),
        ) {
            Icon(
                imageVector = Icons.Rounded.Replay10,
                contentDescription = stringResource(R.string.cast_video_rewind_10),
                modifier = Modifier.size(Dimens.Size.castControlIcon),
            )
        }

        FilledIconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier
                .size(Dimens.Size.castPlayButton)
                .background(Brush.linearGradient(ButtonColors), CircleShape),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
            ),
        ) {
            Crossfade(targetState = isPlaying, label = "playPauseIcon") { playing ->
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(
                        if (playing) R.string.cast_video_pause else R.string.cast_video_play,
                    ),
                    modifier = Modifier.size(Dimens.Size.castPlayIcon),
                )
            }
        }

        FilledTonalIconButton(
            onClick = onForwardClick,
            modifier = Modifier.size(Dimens.Size.castSeekButton),
        ) {
            Icon(
                imageVector = Icons.Rounded.Forward10,
                contentDescription = stringResource(R.string.cast_video_forward_10),
                modifier = Modifier.size(Dimens.Size.castControlIcon),
            )
        }
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
                    playerState = CastPlayerState.PLAYING,
                    positionMs = 754_000L,
                    durationMs = 3_725_000L,
                    volume = 35,
                ),
                onPlayPauseClick = {},
                onRewindClick = {},
                onForwardClick = {},
                onVolumeUpClick = {},
                onVolumeDownClick = {},
                onMuteClick = {},
                onStopCastingClick = {},
                onSeekTo = {},
                failureReason = null,
                onRetry = {},
                onChangeVideo = {},
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
                    playerState = CastPlayerState.PLAYING,
                    positionMs = 12_000L,
                    durationMs = 95_000L,
                    volume = 60,
                    isMuted = true,
                ),
                onPlayPauseClick = {},
                onRewindClick = {},
                onForwardClick = {},
                onVolumeUpClick = {},
                onVolumeDownClick = {},
                onMuteClick = {},
                onStopCastingClick = {},
                onSeekTo = {},
                failureReason = CastFailReason.DEVICE_NOT_FOUND,
                onRetry = {},
                onChangeVideo = {},
            )
        }
    }
}