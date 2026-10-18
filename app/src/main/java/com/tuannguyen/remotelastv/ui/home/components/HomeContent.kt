package com.tuannguyen.remotelastv.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.NumberPadItem
import com.tuannguyen.remotelastv.domain.model.TouchpadEvent
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.ui.common.IconSource
import com.tuannguyen.remotelastv.ui.common.bounceClick
import com.tuannguyen.remotelastv.ui.common.bounceClickNoEffect
import com.tuannguyen.remotelastv.ui.common.logoRes
import com.tuannguyen.remotelastv.ui.common.safeClickableNoEffect
import com.tuannguyen.remotelastv.ui.common.wedgeShape
import com.tuannguyen.remotelastv.ui.home.HomeUiState
import com.tuannguyen.remotelastv.ui.home.KeyboardUiState
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import com.tuannguyen.remotelastv.ui.theme.extendedColors
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    keyboardUiState: KeyboardUiState,
    onSettingsClick: () -> Unit,
    onChangeTelevision: () -> Unit,
    onPowerClick: () -> Unit,
    onAppClick: (KnownApp) -> Unit,
    onRemoteButtonClick: (TvKey) -> Unit,
    onPageSelected: (Int) -> Unit,
    touchpadMode: TouchpadMode,
    touchpadEvent: (TouchpadEvent) -> Unit = {},
    onShowKeyboardSheet: () -> Unit = {},
    onDismissKeyboardSheet: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onKeyboardSubmit: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val pagerState = rememberPagerState(
        initialPage = homeUiState.selectedPage,
        pageCount = { 2 },
    )

    LaunchedEffect(pagerState.currentPage) {
        onPageSelected(pagerState.currentPage)
    }

    LaunchedEffect(homeUiState.selectedPage) {
        if (pagerState.currentPage != homeUiState.selectedPage) {
            pagerState.animateScrollToPage(homeUiState.selectedPage)
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
                    uiState = homeUiState,
                    compact = compact,
                    onSettingsClick = onSettingsClick,
                    onChangeTelevision = onChangeTelevision,
                    onPowerClick = onPowerClick,
                )

                AppRow(apps = homeUiState.shortcutApps, onAppClick = onAppClick)
                ClassicControls(
                    uiState = homeUiState,
                    pagerState = pagerState,
                    compact = compact,
                    onRemoteButtonClick = onRemoteButtonClick,
                    onPageSelected = onPageSelected,
                    touchpadMode = touchpadMode,
                    touchpadEvent = touchpadEvent,
                    showKeyboardSheet = onShowKeyboardSheet
                )
            }
        }
    }

    if (keyboardUiState.showKeyboardSheet) {
        KeyboardInputBottomSheet(
            onDismiss = {
                onDismissKeyboardSheet()
            },
            onSearch = {
                onDismissKeyboardSheet()
                onKeyboardSubmit()
            },
            onQueryChange = { onQueryChange(it) },
        )
    }
}

@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    compact: Boolean,
    onSettingsClick: () -> Unit,
    onChangeTelevision: () -> Unit,
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
                .weight(1f)
                .safeClickableNoEffect(onChangeTelevision)
                .padding(horizontal = Dimens.Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val connectionState = uiState.connectionState
            val isConnected = connectionState is ConnectionState.Connected

            if (isConnected) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.extendedColors.success),
                )

                Spacer(Modifier.width(Dimens.Space.xs))
            }

            Text(
                text = if (connectionState is ConnectionState.Connected) {
                    connectionState.tv.name
                } else {
                    stringResource(R.string.choose_a_tv)
                },
                modifier = Modifier
                    .weight(1f, fill = false),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = if (compact) 16.sp else 20.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.choose_a_tv),
                modifier = Modifier.size(Dimens.Size.iconLarge),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
    apps: List<KnownApp>,
    onAppClick: (KnownApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(items = apps, key = { it }) { app ->
            AppLauncher(app = app, onClick = onAppClick)
        }
    }
}

@Composable
private fun ColumnScope.PadSection(
    pagerState: PagerState,
    compact: Boolean,
    mode: TouchpadMode,
    touchpadEvent: (TouchpadEvent) -> Unit = {},
    navigationMode: NavigationMode,
    onRemoteButtonClick: (TvKey) -> Unit,
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
                    0 -> when (navigationMode) {
                        NavigationMode.D_PAD -> {
                            DPad(
                                diameter = dpadDiameter,
                                onRemoteButtonClick = onRemoteButtonClick,
                            )
                            dpadOverlay?.invoke(this)
                        }

                        NavigationMode.TOUCH_PAD -> TouchPad(
                            onRemoteButtonClick = onRemoteButtonClick,
                            mode = mode,
                            onEvent = touchpadEvent,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Dimens.Space.md),
                        )
                    }

                    1 -> {
                        NumberPad(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Dimens.Space.md),
                            onRemoteKeyClick = onRemoteButtonClick,
                        )
                    }
                }
            }
        }
    }
}

private const val POINTER_SENSITIVITY = 1.5f

@Composable
private fun TouchPad(
    mode: TouchpadMode,
    onEvent: (TouchpadEvent) -> Unit,
    onRemoteButtonClick: (TvKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentOnKey by rememberUpdatedState(onRemoteButtonClick)

    fun Density.positionToKey(
        totalX: Float,
        totalY: Float,
        threshold: Float
    ) {
        if (maxOf(abs(totalX), abs(totalY)) < threshold) return

        val key = if (abs(totalX) > abs(totalY)) {
            if (totalX > 0) TvKey.RIGHT else TvKey.LEFT
        } else {
            if (totalY < 0) TvKey.UP else TvKey.DOWN
        }

        currentOnKey(key)
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    listOf(Color(0xFF9B4CCB), Color(0xFF3E9AEF), Color(0xFF9B4CCB))
                ),
                shape = MaterialTheme.shapes.extraLarge,
            )

            .pointerInput(mode) {
                detectTapGestures(
                    onTap = {
                        when (mode) {
                            TouchpadMode.POINTER -> currentOnEvent(TouchpadEvent.Tap)
                            TouchpadMode.DIRECTIONAL -> currentOnKey(TvKey.OK)
                        }
                    }
                )
            }

            .pointerInput(mode) {
                when (mode) {
                    TouchpadMode.POINTER -> awaitEachGesture {
                        var totalX = 0f
                        var totalY = 0f
                        var isTwoFingerGesture = false
                        var previousPointerCount = 0
                        do {
                            val event = awaitPointerEvent()
                            val pressedChanges = event.changes.filter { it.pressed }
                            val pointerCount = pressedChanges.size
                            when (pointerCount) {
                                1 if !isTwoFingerGesture -> {
                                    val change = pressedChanges.first()

                                    if (change.position != change.previousPosition) {
                                        val delta = change.position - change.previousPosition

                                        change.consume()

                                        currentOnEvent(
                                            TouchpadEvent.Move(
                                                delta.x * POINTER_SENSITIVITY,
                                                delta.y * POINTER_SENSITIVITY,
                                            )
                                        )
                                    }
                                }

                                2 -> {
                                    if (previousPointerCount < 2) {
                                        isTwoFingerGesture = true
                                        totalX = 0f
                                        totalY = 0f
                                    } else {
                                        val first = pressedChanges[0]
                                        val second = pressedChanges[1]

                                        val delta1 = first.position - first.previousPosition
                                        val delta2 = second.position - second.previousPosition

                                        totalX += (delta1.x + delta2.x) / 2f
                                        totalY += (delta1.y + delta2.y) / 2f

                                        first.consume()
                                        second.consume()
                                    }
                                }
                            }

                            previousPointerCount = pointerCount
                        } while (event.changes.any { it.pressed })

                        if (isTwoFingerGesture) {
                            val threshold = 40.dp.toPx()
                            positionToKey(totalX, totalY, threshold)
                        }
                    }

                    TouchpadMode.DIRECTIONAL -> {
                        var totalX = 0f
                        var totalY = 0f

                        detectDragGestures(
                            onDragStart = { totalX = 0f; totalY = 0f },
                            onDragCancel = { totalX = 0f; totalY = 0f },
                            onDrag = { change, drag ->
                                change.consume()
                                totalX += drag.x
                                totalY += drag.y
                            },
                            onDragEnd = {
                                val threshold = 50.dp.toPx()
                                positionToKey(totalX, totalY, threshold)
                            },
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {}
}

@Composable
private fun PageDots(
    selectedPage: Int,
    onPageSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.padding(bottom = Dimens.Space.lg),
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
    onRemoteButtonClick: (TvKey) -> Unit,
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
                .weight(0.9f)
                .height(if (compact) 160.dp else 180.dp),
            onTop = { onRemoteButtonClick(TvKey.VOLUME_UP) },
            onBottom = { onRemoteButtonClick(TvKey.VOLUME_DOWN) },
        )
        Column(
            modifier = Modifier.weight(3f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = centerContent,
        )
        VerticalControl(
            iconTop = Icons.Default.KeyboardArrowUp,
            middle = "CH",
            iconBottom = Icons.Default.KeyboardArrowDown,
            modifier = Modifier
                .weight(0.9f)
                .height(if (compact) 160.dp else 180.dp),
            onTop = { onRemoteButtonClick(TvKey.CHANNEL_UP) },
            onBottom = { onRemoteButtonClick(TvKey.CHANNEL_DOWN) },
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
    onRemoteButtonClick: (TvKey) -> Unit,
    touchpadMode: TouchpadMode,
    touchpadEvent: (TouchpadEvent) -> Unit = {},
    onPageSelected: (Int) -> Unit,
    showKeyboardSheet: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    PadSection(
        pagerState = pagerState,
        compact = compact,
        navigationMode = uiState.navigationMode,
        mode = touchpadMode,
        touchpadEvent = touchpadEvent,
        onRemoteButtonClick = onRemoteButtonClick,
    )

    PageDots(selectedPage = uiState.selectedPage, onPageSelected = onPageSelected)

    BottomControls(compact = compact, onRemoteButtonClick = onRemoteButtonClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundControl(
                icon = IconSource.Vector(Icons.Default.Keyboard),
                description = "Keyboard",
                modifier = Modifier.weight(1f),
            ) { showKeyboardSheet() }
            RoundControl(
                icon = IconSource.Resource(R.drawable.ic_home),
                description = "Home",
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(TvKey.HOME) }

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

        if (uiState.showMediaButtons) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RoundControl(
                    icon = IconSource.Resource(R.drawable.ic_previous),
                    description = "Skip Previous",
                    modifier = Modifier.weight(1f),
                ) {
                    onRemoteButtonClick(TvKey.PREVIOUS)
                }
                RoundControl(
                    icon = IconSource.Resource(R.drawable.ic_pause_play),
                    description = "Play/Pause",
                    modifier = Modifier.weight(1f),
                ) { onRemoteButtonClick(TvKey.PLAY_PAUSE) }

                RoundControl(
                    icon = IconSource.Resource(R.drawable.ic_next),
                    description = "Skip Next",
                    modifier = Modifier.weight(1f),
                ) { onRemoteButtonClick(TvKey.NEXT) }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MuteControl(
                isMuted = uiState.isMuted,
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(TvKey.MUTE) }
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
            ) { onRemoteButtonClick(TvKey.BACK) }
        }
    }
}

private val RingStroke = 2.2.dp
private val RingColors = listOf(
    Color(0xFF9B4CCB),
    Color(0xFF3E9AEF),
    Color(0xFF9B4CCB),
)

@Composable
private fun DPad(
    diameter: Dp,
    onRemoteButtonClick: (TvKey) -> Unit,
) {
    val ringPadding = Dimens.Space.sm
    val disc = diameter - ringPadding * 2
    val edge = disc * 0.12f

    Box(
        modifier = Modifier
            .size(diameter)
            .padding(ringPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f),
                            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.25f),
                        )
                    )
                )
                .border(
                    width = RingStroke,
                    brush = Brush.sweepGradient(RingColors),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
        ) {
            DirectionKey(
                icon = Icons.Default.KeyboardArrowUp,
                command = TvKey.UP,
                centerAngle = 270f,
                iconAlignment = Alignment.TopCenter,
                iconPadding = Modifier.padding(top = edge),
                onRemoteButtonClick = onRemoteButtonClick,
            )
            DirectionKey(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                command = TvKey.LEFT,
                centerAngle = 180f,
                iconAlignment = Alignment.CenterStart,
                iconPadding = Modifier.padding(start = edge),
                onRemoteButtonClick = onRemoteButtonClick,
            )
            DirectionKey(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                command = TvKey.RIGHT,
                centerAngle = 0f,
                iconAlignment = Alignment.CenterEnd,
                iconPadding = Modifier.padding(end = edge),
                onRemoteButtonClick = onRemoteButtonClick,
            )
            DirectionKey(
                icon = Icons.Default.KeyboardArrowDown,
                command = TvKey.DOWN,
                centerAngle = 90f,
                iconAlignment = Alignment.BottomCenter,
                iconPadding = Modifier.padding(bottom = edge),
                onRemoteButtonClick = onRemoteButtonClick,
            )
        }

        Box(
            modifier = Modifier
                .size(disc * 0.28f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(RingColors),
                    shape = CircleShape,
                )
                .bounceClick { onRemoteButtonClick(TvKey.OK) },
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
    onRemoteKeyClick: (TvKey) -> Unit,
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
                                        TvKey.valueOf("NUMBER_${item.value}")
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
                                    onRemoteKeyClick(TvKey.DELETE)
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
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isDelete) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
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
    app: KnownApp, onClick: (KnownApp) -> Unit, modifier: Modifier = Modifier
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
    command: TvKey,
    centerAngle: Float,
    iconAlignment: Alignment,
    iconPadding: Modifier,
    onRemoteButtonClick: (TvKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
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
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
                    onRemoteButtonClick(command)
                }),
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
                .bounceClick(onClick = onTop, shape = CircleShape, debounceMs = 0)
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
                .bounceClick(shape = CircleShape, onClick = onBottom, debounceMs = 0)
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
private fun HomeContentClassicPreview() {
    RemoteLASTvTheme {
        HomeContent(
            homeUiState = HomeUiState(),
            onSettingsClick = {},
            onChangeTelevision = {},
            onPowerClick = {},
            onAppClick = {},
            onRemoteButtonClick = {},
            onPageSelected = {},
            keyboardUiState = KeyboardUiState(),
            touchpadMode = TouchpadMode.DIRECTIONAL
        )
    }
}