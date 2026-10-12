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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.NumberPadItem
import com.tuannguyen.remotelastv.domain.model.RemoteKey
import com.tuannguyen.remotelastv.domain.model.RemoteLayout
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
import kotlinx.coroutines.launch

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onSettingsClick: () -> Unit,
    onDeviceClick: () -> Unit,
    onPowerClick: () -> Unit,
    onAppClick: (TvApp) -> Unit,
    onRemoteButtonClick: (RemoteKey) -> Unit,
    onPageSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedPage,
        pageCount = { 2 },
    )

    LaunchedEffect(pagerState.currentPage) {
        onPageSelected(pagerState.currentPage)
    }

    LaunchedEffect(uiState.selectedPage) {
        if (pagerState.currentPage != uiState.selectedPage) {
            pagerState.animateScrollToPage(uiState.selectedPage)
        }
    }

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
                HomeHeader(
                    uiState = uiState,
                    compact = compact,
                    onSettingsClick = onSettingsClick,
                    onDeviceClick = onDeviceClick,
                    onPowerClick = onPowerClick,
                )

                AppRow(compact = compact, onAppClick = onAppClick)

                when (uiState.layout) {
                    RemoteLayout.Classic -> ClassicControls(
                        uiState = uiState,
                        pagerState = pagerState,
                        compact = compact,
                        onRemoteButtonClick = onRemoteButtonClick,
                        onPageSelected = onPageSelected,
                    )

                    RemoteLayout.Media -> MediaControls(
                        uiState = uiState,
                        pagerState = pagerState,
                        compact = compact,
                        onRemoteButtonClick = onRemoteButtonClick,
                        onPageSelected = onPageSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    compact: Boolean,
    onSettingsClick: () -> Unit,
    onDeviceClick: () -> Unit,
    onPowerClick: () -> Unit,
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
}

@Composable
private fun AppRow(
    compact: Boolean,
    onAppClick: (TvApp) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (compact) 10.dp else 28.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        AppLauncher(app = TvApp.NETFLIX, onClick = onAppClick)
        AppLauncher(app = TvApp.YOUTUBE, onClick = onAppClick)
        AppLauncher(app = TvApp.PRIME_VIDEO, onClick = onAppClick)
        AppLauncher(app = TvApp.DISNEY_PLUS, onClick = onAppClick)
        AppLauncher(app = TvApp.DISNEY_PLUS, onClick = onAppClick)
    }
}

@Composable
private fun ColumnScope.PadSection(
    pagerState: PagerState,
    compact: Boolean,
    onRemoteButtonClick: (RemoteKey) -> Unit,
    dpadOverlay: (@Composable BoxScope.() -> Unit)? = null,
) {
    Spacer(Modifier.height(if (compact) 12.dp else 16.dp))

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center,
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val dpadDiameter = minOf(availableWidth, availableHeight)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = Dimens.Space.md,
            beyondViewportPageCount = 0,
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when (page) {
                    0 -> {
                        DPad(
                            diameter = dpadDiameter,
                            onRemoteButtonClick = onRemoteButtonClick,
                        )
                        dpadOverlay?.invoke(this)
                    }

                    1 -> {
                        NumberPad(
                            modifier = Modifier.fillMaxSize().padding( Dimens.Space.md),
                            onRemoteKeyClick = onRemoteButtonClick,
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))
}

@Composable
private fun PageDots(
    selectedPage: Int,
    onPageSelected: (Int) -> Unit,
) {
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
                        if (selectedPage == index) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                    .bounceClick { onPageSelected(index) })
        }
    }
}

@Composable
private fun BottomControls(
    compact: Boolean,
    onRemoteButtonClick: (RemoteKey) -> Unit,
    centerContent: @Composable ColumnScope.() -> Unit,
) {
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
            onTop = { onRemoteButtonClick(RemoteKey.VOLUME_UP) },
            onBottom = { onRemoteButtonClick(RemoteKey.VOLUME_DOWN) },
        )
        Column(
            modifier = Modifier.weight(3.1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = centerContent,
        )
        VerticalControl(
            iconTop = Icons.Default.KeyboardArrowUp,
            middle = "CH",
            iconBottom = Icons.Default.KeyboardArrowDown,
            modifier = Modifier
                .weight(0.88f)
                .height(if (compact) 170.dp else 200.dp),
            onTop = { onRemoteButtonClick(RemoteKey.CHANNEL_UP) },
            onBottom = { onRemoteButtonClick(RemoteKey.CHANNEL_DOWN) },
        )
    }
}

@Composable
private fun MuteControl(
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (!isMuted) {
        RoundControl(
            icon = IconSource.Resource(R.drawable.ic_unmute),
            description = "Mute",
            modifier = modifier,
            onClick = onClick,
        )
    } else {
        RoundControl(
            icon = IconSource.Resource(R.drawable.ic_mute),
            description = "Unmute",
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.error,
            onClick = onClick,
        )
    }
}

@Composable
private fun ColumnScope.ClassicControls(
    uiState: HomeUiState,
    pagerState: PagerState,
    compact: Boolean,
    onRemoteButtonClick: (RemoteKey) -> Unit,
    onPageSelected: (Int) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    PadSection(
        pagerState = pagerState,
        compact = compact,
        onRemoteButtonClick = onRemoteButtonClick,
    )

    PageDots(selectedPage = uiState.selectedPage, onPageSelected = onPageSelected)

    BottomControls(compact = compact, onRemoteButtonClick = onRemoteButtonClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundControl(
                icon = IconSource.Vector(Icons.Default.Keyboard),
                description = "Keyboard",
                modifier = Modifier.weight(1f),
            ) {
                // TODO: show keyboard
            }
            RoundControl(
                icon = IconSource.Resource(R.drawable.ic_home),
                description = "Home",
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(RemoteKey.HOME) }

            val isDPadPage = pagerState.currentPage == 0

            RoundControl(
                icon = if (isDPadPage) IconSource.Vector(Icons.Default.Apps)
                else IconSource.Resource(R.drawable.ic_dpad),
                description = if (isDPadPage) "Number Pad" else "Directional Pad",
                modifier = Modifier.weight(1f),
            ) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(if (isDPadPage) 1 else 0)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MuteControl(
                isMuted = uiState.isMuted,
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(RemoteKey.MUTE) }
            RoundControl(
                icon = IconSource.Vector(Icons.Default.Mic),
                description = "Voice",
                modifier = Modifier.weight(1f),
            ) {
                // TODO: voice
            }
            RoundControl(
                icon = IconSource.Resource(R.drawable.ic_return),
                description = "Back",
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(RemoteKey.BACK) }
        }
    }
}

private val CornerButtonSize = 56.dp

@Composable
private fun ColumnScope.MediaControls(
    uiState: HomeUiState,
    pagerState: PagerState,
    compact: Boolean,
    onRemoteButtonClick: (RemoteKey) -> Unit,
    onPageSelected: (Int) -> Unit,
) {
    PadSection(
        pagerState = pagerState,
        compact = compact,
        onRemoteButtonClick = onRemoteButtonClick,
        dpadOverlay = {
            RoundControl(
                icon = IconSource.Vector(Icons.Default.Mic),
                description = "Voice",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = Dimens.Space.md)
                    .size(CornerButtonSize),
            ) {
                // TODO: voice
            }
            RoundControl(
                icon = IconSource.Resource(R.drawable.ic_return),
                description = "Back",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Dimens.Space.md)
                    .size(CornerButtonSize),
            ) { onRemoteButtonClick(RemoteKey.BACK) }

            MuteControl(
                isMuted = uiState.isMuted,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = Dimens.Space.md)
                    .size(CornerButtonSize),
            ) { onRemoteButtonClick(RemoteKey.MUTE) }
        },
    )

    PageDots(selectedPage = uiState.selectedPage, onPageSelected = onPageSelected)

    BottomControls(compact = compact, onRemoteButtonClick = onRemoteButtonClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundControl(
                icon = IconSource.Vector(Icons.Default.Keyboard),
                description = "Keyboard",
                modifier = Modifier.weight(1f),
            ) {
                // TODO: show keyboard
            }
            RoundControl(
                icon = IconSource.Resource(R.drawable.ic_home),
                description = "Home",
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(RemoteKey.HOME) }
            MuteControl(
                isMuted = uiState.isMuted,
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(RemoteKey.MUTE) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundControl(
                icon = IconSource.Vector(Icons.Default.SkipPrevious),
                description = "Previous",
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(RemoteKey.PREVIOUS) }
            RoundControl(
                icon = IconSource.Vector(Icons.Default.Pause),
                description = "Play/Pause",
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(RemoteKey.PLAY_PAUSE) }
            RoundControl(
                icon = IconSource.Vector(Icons.Default.SkipNext),
                description = "Next",
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(RemoteKey.NEXT) }
        }
    }
}

@Composable
private fun DPad(
    diameter: Dp,
    onRemoteButtonClick: (RemoteKey) -> Unit,
) {
    Box(
        modifier = Modifier.size(diameter),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.Space.lg),
        ) {
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
            modifier = Modifier
                .fillMaxSize(0.99f)
                .padding(1.dp)
                .padding(Dimens.Space.lg)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceContainerHigh
                                .copy(alpha = 0.78f),
                            MaterialTheme.colorScheme.surfaceContainerLow
                                .copy(alpha = 0.25f),
                        )
                    )
                ),
        )

        val edge = diameter * 0.12f

        DirectionKey(
            Icons.Default.KeyboardArrowUp,
            RemoteKey.UP,
            270f,
            Alignment.TopCenter,
            Modifier.padding(top = edge),
            onRemoteButtonClick,
        )

        DirectionKey(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            RemoteKey.LEFT,
            180f,
            Alignment.CenterStart,
            Modifier.padding(start = edge),
            onRemoteButtonClick,
        )

        DirectionKey(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            RemoteKey.RIGHT,
            0f,
            Alignment.CenterEnd,
            Modifier.padding(end = edge),
            onRemoteButtonClick,
        )

        DirectionKey(
            Icons.Default.KeyboardArrowDown,
            RemoteKey.DOWN,
            90f,
            Alignment.BottomCenter,
            Modifier.padding(bottom = edge),
            onRemoteButtonClick,
        )

        Box(
            modifier = Modifier
                .size(diameter * 0.28f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFF9B4CCB),
                            Color(0xFF3E9AEF),
                            Color(0xFF9B4CCB),
                        )
                    ),
                    shape = CircleShape,
                )
                .bounceClick {
                    onRemoteButtonClick(RemoteKey.OK)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "OK",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private val numberPadRows = listOf(
    listOf(
        NumberPadItem.Number(1),
        NumberPadItem.Number(2),
        NumberPadItem.Number(3),
    ),
    listOf(
        NumberPadItem.Number(4),
        NumberPadItem.Number(5),
        NumberPadItem.Number(6),
    ),
    listOf(
        NumberPadItem.Number(7),
        NumberPadItem.Number(8),
        NumberPadItem.Number(9),
    ),
    listOf(
        NumberPadItem.Empty,
        NumberPadItem.Number(0),
        NumberPadItem.Delete,
    ),
)

@Composable
private fun NumberPad(
    onRemoteKeyClick: (RemoteKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        numberPadRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { item ->
                    when (item) {
                        is NumberPadItem.Number -> {
                            NumberPadKey(
                                key = item.value.toString(),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = {
                                    onRemoteKeyClick(
                                        RemoteKey.valueOf("NUMBER_${item.value}")
                                    )
                                },
                            )
                        }

                        NumberPadItem.Delete -> {
                            NumberPadKey(
                                key = "delete",
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = {
                                    onRemoteKeyClick(RemoteKey.DELETE)
                                },
                            )
                        }

                        NumberPadItem.Empty -> {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPadKey(
    key: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val isDelete = key == "delete"

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .then(
                if (isDelete) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(
                            alpha = 0.35f
                        ),
                        shape = MaterialTheme.shapes.large,
                    )
                } else {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large,
                    )
                }
            )
            .safeClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isDelete) {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Delete",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        } else if (key.isNotEmpty()) {
            Text(
                text = key,
                fontSize = 25.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AppLauncher(
    app: TvApp, onClick: (TvApp) -> Unit, modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(app.logoRes()),
        contentDescription = app.name,
        modifier = modifier
            .bounceClickNoEffect(
                onClick = { onClick(app) })
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
    command: RemoteKey,
    centerAngle: Float,
    iconAlignment: Alignment,
    iconPadding: Modifier,
    onRemoteButtonClick: (RemoteKey) -> Unit,
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
                onClick = { onRemoteButtonClick(command) }),
        contentAlignment = iconAlignment,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = command.name,
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

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeContentClassicPreview() {
    RemoteLASTvTheme {
        HomeContent(
            uiState = HomeUiState(layout = RemoteLayout.Classic),
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
private fun HomeContentMediaPreview() {
    RemoteLASTvTheme {
        HomeContent(
            uiState = HomeUiState(layout = RemoteLayout.Media),
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

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun HomeContentMediaCompactPreview() {
    RemoteLASTvTheme {
        HomeContent(
            uiState = HomeUiState(layout = RemoteLayout.Media),
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