package com.tuannguyen.remotelastv.ui.home.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.ui.common.logoRes
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

private val DialogShape = RoundedCornerShape(28.dp)
private val TileShape = RoundedCornerShape(20.dp)
private val LogoShape = RoundedCornerShape(14.dp)
private val TilePadding = 10.dp
private val GridMaxHeight = 420.dp

@Composable
fun MoreAppsDialog(
    apps: List<KnownApp>,
    onAppClick: (KnownApp) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Space.lg),
            shape = DialogShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(bottom = Dimens.Space.md)
            ) {
                DialogHeader(onClose = onDismiss)

                if (apps.isEmpty()) {
                    EmptyState()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = GridMaxHeight),
                        contentPadding = PaddingValues(
                            start = Dimens.Space.lg,
                            end = Dimens.Space.lg,
                            top = Dimens.Space.xs,
                            bottom = Dimens.Space.lg,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Space.md),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Space.lg),
                    ) {
                        items(
                            items = apps,
                            key = { app -> app.name },
                        ) { app ->
                            AppGridItem(
                                app = app,
                                onClick = { onAppClick(app) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.Space.lg,
                end = Dimens.Space.xs,
                top = Dimens.Space.xs,
                bottom = Dimens.Space.xs,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_available_apps),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Dimens.Space.md),
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.common_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.Space.lg,
                end = Dimens.Space.lg,
                top = Dimens.Space.xs,
                bottom = Dimens.Space.lg,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.home_no_apps_available),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = Dimens.Space.lg),
        )
    }
}

@Composable
private fun AppGridItem(
    app: KnownApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "appTileScale",
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(TileShape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .padding(vertical = Dimens.Space.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs),
    ) {
        // Đế của icon: nền tonal + viền mảnh để logo nào (sáng/tối) cũng nổi rõ.
        Surface(
            shape = TileShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            ),
        ) {
            Image(
                painter = painterResource(app.logoRes()),
                contentDescription = null, // tên app đã hiển thị bên dưới
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(TilePadding)
                    .size(Dimens.Size.appLogoSize)
                    .clip(LogoShape),
            )
        }

        Text(
            text = app.displayName,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(
    name = "More Apps Dialog",
    showBackground = true,
)
@Composable
private fun MoreAppsDialogPreview() {
    RemoteLASTvTheme {
        MoreAppsDialog(
            apps = KnownApp.entries,
            onAppClick = {},
            onDismiss = {},
        )
    }
}

@Preview(
    name = "More Apps Dialog - Empty",
    showBackground = true,
)
@Composable
private fun MoreAppsDialogEmptyPreview() {
    RemoteLASTvTheme {
        MoreAppsDialog(
            apps = emptyList(),
            onAppClick = {},
            onDismiss = {},
        )
    }
}