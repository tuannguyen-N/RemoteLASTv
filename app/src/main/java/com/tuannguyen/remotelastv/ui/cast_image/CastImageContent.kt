package com.tuannguyen.remotelastv.ui.cast_image

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.domain.model.CastImagePhase
import com.tuannguyen.remotelastv.ui.cast_video.ConnectionFailureOverlay
import com.tuannguyen.remotelastv.ui.common.messageResource
import com.tuannguyen.remotelastv.ui.components.CastingToHeader
import com.tuannguyen.remotelastv.ui.components.DialogLoading
import com.tuannguyen.remotelastv.ui.theme.ButtonColors
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

private val PrimaryActionHeight = 56.dp
private const val MAX_DECODE_SIDE = 2048

@Composable
fun CastImageContent(
    uiState: CastImageUiState,
    onChangeImageClick: () -> Unit,
    onStopCastingClick: () -> Unit,
    failureReason: CastFailReason?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    image: @Composable BoxScope.() -> Unit = { ImagePlaceholder() },
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
                CastImageStage(
                    imageUri = uiState.imageUri,
                    image = image,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )

                Spacer(Modifier.height(Dimens.Space.md))

                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Dimens.Space.lg))

                ChooseAnotherButton(
                    enabled = uiState.phase != CastImagePhase.LOADING,
                    onClick = onChangeImageClick,
                )
            }

            if (uiState.phase == CastImagePhase.LOADING) {
                DialogLoading(
                    message = stringResource(R.string.cast_image_loading_message),
                    modal = false,
                )
            }

            failureReason?.let { reason ->
                ConnectionFailureOverlay(
                    title = stringResource(R.string.cast_image_failure_title),
                    errorMessage = stringResource(reason.messageResource()),
                    retryLabel = stringResource(R.string.cast_image_try_again),
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun CastImageStage(
    imageUri: Uri?,
    image: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.Radius.card)

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), shape),
    ) {
        Crossfade(
            targetState = imageUri,
            animationSpec = tween(durationMillis = 300),
            label = "castImage",
        ) { uri ->
            Box(Modifier.fillMaxSize()) {
                if (uri == null) image() else CastedImage(uri)
            }
        }
    }
}

@Composable
private fun ChooseAnotherButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.Radius.controlSurface)

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(horizontal = Dimens.Space.lg),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(PrimaryActionHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .background(Brush.linearGradient(ButtonColors)),
    ) {
        Icon(
            imageVector = Icons.Rounded.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(Dimens.Size.iconMedium),
        )
        Spacer(Modifier.width(Dimens.Space.sm))
        Text(
            text = stringResource(R.string.cast_image_choose_another),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun BoxScope.ImagePlaceholder() {
    Icon(
        imageVector = Icons.Rounded.Image,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .align(Alignment.Center)
            .size(Dimens.Size.castThumbnailPlaceholder),
    )
}

@Composable
private fun CastedImage(uri: Uri) {
    SubcomposeAsyncImage(
        model = uri,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        loading = { ImagePlaceholder() },
        error = { ImagePlaceholder() },
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true, name = "Cast image - placeholder")
@Composable
private fun CastImageContentPreview() {
    RemoteLASTvTheme {
        CastImageContent(
            uiState = CastImageUiState(
                tvName = "Living room TV",
                title = "IMG_2041.jpg",
            ),
            onChangeImageClick = {},
            onStopCastingClick = {},
            failureReason = null,
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "Cast image - loading")
@Composable
private fun CastImageContentLoadingPreview() {
    RemoteLASTvTheme {
        CastImageContent(
            uiState = CastImageUiState(
                tvName = "Living room TV",
                title = "IMGádfasdfasdfasdfasdfasdfasdf_2041.jpg",
                phase = CastImagePhase.LOADING,
            ),
            onChangeImageClick = {},
            onStopCastingClick = {},
            failureReason = null,
            onRetry = {},
        )
    }
}