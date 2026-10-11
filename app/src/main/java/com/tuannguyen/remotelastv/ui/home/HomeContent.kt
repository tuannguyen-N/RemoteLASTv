package com.tuannguyen.remotelastv.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.TvApp
import com.tuannguyen.remotelastv.ui.common.IconSource
import com.tuannguyen.remotelastv.ui.common.bounceClick
import com.tuannguyen.remotelastv.ui.common.bounceClickNoEffect
import com.tuannguyen.remotelastv.ui.common.clickableNoEffect
import com.tuannguyen.remotelastv.ui.common.logoRes
import com.tuannguyen.remotelastv.ui.common.safeClickable
import com.tuannguyen.remotelastv.ui.common.wedgeShape
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onSettingsClick: () -> Unit,
    onDeviceClick: () -> Unit,
    onPowerClick: () -> Unit,
    onAppClick: (TvApp) -> Unit,
    onRemoteButtonClick: (String) -> Unit,
    onPageSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val compact = this.maxHeight < 700.dp
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 64.dp else 82.dp)
                        .padding(horizontal = Dimens.Space.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    HeaderIcon(
                        iconRes = R.drawable.ic_setting,

                        description = "Settings",
                        onClick = onSettingsClick,
                    )
                    Row(
                        modifier = Modifier
                            .clickableNoEffect(onDeviceClick)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = uiState.connectedDeviceName,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "DropDown",
                            modifier = Modifier.size(Dimens.Size.iconLarge)
                        )
                    }
                    HeaderIcon(
                        iconRes = R.drawable.ic_on_off,
                        description = "Power",
                        onClick = onPowerClick,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (compact) 10.dp else 28.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    AppLauncher(
                        app = TvApp.NETFLIX,
                        onClick = onAppClick
                    )
                    AppLauncher(
                        app = TvApp.YOUTUBE,
                        onClick = onAppClick
                    )
                    AppLauncher(
                        app = TvApp.PRIME_VIDEO,
                        onClick = onAppClick
                    )
                    AppLauncher(
                        app = TvApp.DISNEY_PLUS,
                        onClick = onAppClick
                    )
                    AppLauncher(
                        app = TvApp.DISNEY_PLUS,
                        onClick = onAppClick
                    )
                }

                Spacer(Modifier.height(if (compact) 12.dp else 16.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.Space.lg)
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center,
                ) {
                    val diameter = minOf(this.maxWidth, this.maxHeight).coerceAtMost(420.dp)
                    Box(Modifier.size(diameter), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val stroke = 2.2.dp.toPx()
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF9B4CCB),
                                        Color(0xFF3E9AEF),
                                        Color(0xFF9B4CCB),
                                    )
                                ),
                                radius = size.minDimension / 2 - stroke / 2,
                                style = Stroke(width = stroke),
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxSize(0.99f)
                                .padding(1.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                                                alpha = 0.78f
                                            ),
                                            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.25f),
                                        )
                                    )
                                )
                        )
                        val edge = diameter * 0.12f
                        DirectionKey(
                            Icons.Default.KeyboardArrowUp, "Up",
                            270f,
                            Alignment.TopCenter,
                            Modifier.padding(top = edge), onRemoteButtonClick
                        )
                        DirectionKey(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            "Left",
                            180f,
                            Alignment.CenterStart,
                            Modifier.padding(start = edge),
                            onRemoteButtonClick
                        )
                        DirectionKey(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            "Right",
                            0f,
                            Alignment.CenterEnd,
                            Modifier.padding(end = edge), onRemoteButtonClick
                        )
                        DirectionKey(
                            Icons.Default.KeyboardArrowDown,
                            "Down",
                            90f,
                            Alignment.BottomCenter,
                            Modifier.padding(bottom = edge),
                            onRemoteButtonClick
                        )
                        Box(
                            Modifier
                                .size(diameter * 0.28f)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            Color(0xFF9B4CCB),
                                            Color(0xFF3E9AEF),
                                            Color(0xFF9B4CCB)
                                        )
                                    ),
                                    shape = CircleShape,
                                )
                                .bounceClick { onRemoteButtonClick("OK") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(" ", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(2) { index ->
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.selectedPage == index) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                                .bounceClick { onPageSelected(index) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .padding(horizontal = Dimens.Space.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    VerticalControl(
                        iconTop = Icons.Default.Add,
                        middle = "VOL",
                        iconBottom = Icons.Default.Remove,
                        modifier = Modifier
                            .weight(0.88f)
                            .height(if (compact) 170.dp else 200.dp),
                        onTop = { onRemoteButtonClick("Volume up") },
                        onBottom = { onRemoteButtonClick("Volume down") },
                    )
                    Column(
                        modifier = Modifier.weight(3.1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            RoundControl(
                                icon = IconSource.Vector(Icons.Default.Keyboard),
                                "Keyboard",
                                Modifier.weight(1f)
                            ) { onRemoteButtonClick("Keyboard") }
                            RoundControl(
                                icon = IconSource.Resource(R.drawable.ic_home),
                                "Home",
                                Modifier.weight(1f)
                            ) { onRemoteButtonClick("Home") }
                            RoundControl(
                                icon = IconSource.Vector(Icons.Default.Apps),
                                "Apps",
                                Modifier.weight(1f)
                            ) { onRemoteButtonClick("Apps") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!uiState.isMuted) {
                                RoundControl(
                                    icon = IconSource.Resource(R.drawable.ic_unmute),
                                    "Mute",
                                    Modifier.weight(1f)
                                ) { onRemoteButtonClick("Mute") }
                            } else {
                                RoundControl(
                                    icon = IconSource.Resource(R.drawable.ic_mute),
                                    description = "Mute",
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.error,
                                ) { onRemoteButtonClick("UnMute") }
                            }
                            RoundControl(
                                icon = IconSource.Vector(Icons.Default.Mic),
                                "Voice",
                                Modifier.weight(1f)
                            ) { onRemoteButtonClick("Voice") }
                            RoundControl(
                                icon = IconSource.Resource(R.drawable.ic_return),
                                "Back",
                                Modifier.weight(1f)
                            ) { onRemoteButtonClick("Back") }
                        }
                    }
                    VerticalControl(
                        iconTop = Icons.Default.KeyboardArrowUp,
                        middle = "CH",
                        iconBottom = Icons.Default.KeyboardArrowDown,
                        modifier = Modifier
                            .weight(0.88f)
                            .height(if (compact) 170.dp else 200.dp),
                        onTop = { onRemoteButtonClick("Channel up") },
                        onBottom = { onRemoteButtonClick("Channel down") },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppLauncher(
    app: TvApp,
    onClick: (TvApp) -> Unit,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(app.logoRes()),
        contentDescription = app.name,
        modifier = modifier
            .bounceClickNoEffect(
                onClick = { onClick(app) }
            )
            .padding(start = 16.dp)
            .size(80.dp)
            .clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun HeaderIcon(
    iconRes: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .bounceClickNoEffect(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(Dimens.Size.iconLarge),
        )
    }
}

@Composable
private fun DirectionKey(
    icon: ImageVector,
    command: String,
    centerAngle: Float,
    iconAlignment: Alignment,
    iconPadding: Modifier,
    onRemoteButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = remember(centerAngle) { wedgeShape(centerAngle) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val highlightAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0f,
        animationSpec = tween(durationMillis = if (isPressed) 120 else 250),
        label = "highlight"
    )
    val highlightColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .drawBehind {
                drawRect(highlightColor.copy(alpha = highlightAlpha))
            }
            .safeClickable(
                onClick = { onRemoteButtonClick(command) }
            ),
        contentAlignment = iconAlignment,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = command,
            modifier = iconPadding.size(Dimens.Size.iconLarge),
        )
    }
}

@Composable
private fun RoundControl(
    icon: IconSource,
    description: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .bounceClick(shape = CircleShape, onClick = onClick)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        when (icon) {
            is IconSource.Vector -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = description,
                    modifier = Modifier.size(Dimens.Size.iconLarge)
                )
            }

            is IconSource.Resource -> {
                Icon(
                    painter = painterResource(id = icon.resId),
                    contentDescription = description,
                    modifier = Modifier.size(Dimens.Size.iconLarge)
                )
            }
        }
    }
}

@Composable
private fun VerticalControl(
    iconTop: ImageVector,
    middle: String,
    iconBottom: ImageVector,
    modifier: Modifier = Modifier,
    onTop: () -> Unit,
    onBottom: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        Box(
            Modifier
                .bounceClick(onClick = onTop, shape = CircleShape)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconTop,
                contentDescription = "IconTop$middle",
                modifier = Modifier.size(Dimens.Size.iconLarge),
            )
        }
        Text(
            middle,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Box(
            Modifier
                .bounceClick(shape = CircleShape, onClick = onBottom)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconBottom,
                contentDescription = "IconBottom$middle",
                modifier = Modifier.size(Dimens.Size.iconLarge),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeContentSuccessPreview() {
    RemoteLASTvTheme {
        HomeContent(
            uiState = HomeUiState(),
            onSettingsClick = {},
            onDeviceClick = {},
            onPowerClick = {},
            onAppClick = {},
            onRemoteButtonClick = {},
            onPageSelected = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeContentLoadingPreview() {
    RemoteLASTvTheme {
        HomeContent(
            uiState = HomeUiState(isLoading = true),
            onSettingsClick = {},
            onDeviceClick = {},
            onPowerClick = {},
            onAppClick = {},
            onRemoteButtonClick = {},
            onPageSelected = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeContentEmptyPreview() {
    RemoteLASTvTheme {
        HomeContent(
            uiState = HomeUiState(isEmpty = true),
            onSettingsClick = {},
            onDeviceClick = {},
            onPowerClick = {},
            onAppClick = {},
            onRemoteButtonClick = {},
            onPageSelected = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeContentErrorPreview() {
    RemoteLASTvTheme {
        HomeContent(
            uiState = HomeUiState(errorMessage = "Unable to connect"),
            onSettingsClick = {},
            onDeviceClick = {},
            onPowerClick = {},
            onAppClick = {},
            onRemoteButtonClick = {},
            onPageSelected = {},
            onRetry = {},
        )
    }
}