package com.tuannguyen.remotelastv.ui.remote.components


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.NumberPadItem
import com.tuannguyen.remotelastv.domain.model.TouchpadEvent
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.ui.common.IconSource
import com.tuannguyen.remotelastv.ui.common.bounceClick
import com.tuannguyen.remotelastv.ui.common.bounceClickRepeatable
import com.tuannguyen.remotelastv.ui.common.repeatableClick
import com.tuannguyen.remotelastv.ui.common.wedgeShape
import com.tuannguyen.remotelastv.ui.remote.RemoteUiState
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import com.tuannguyen.remotelastv.ui.theme.RingColors
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private val RingStroke = 2.dp

@Composable
private fun hairline(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

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
    Spacer(Modifier.height(if (compact) Dimens.Space.sm else Dimens.Space.md))

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

    fun positionToKey(
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
                width = RingStroke,
                brush = Brush.sweepGradient(RingColors),
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
    ) {
    }
}

@Composable
private fun PageDots(
    selectedPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(bottom = Dimens.Space.lg),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(2) { index ->
            val selected = selectedPage == index
            val dotWidth by animateDpAsState(
                targetValue = if (selected) Dimens.Space.lg else Dimens.Space.xs,
                label = "dotWidth",
            )
            Box(
                Modifier
                    .height(Dimens.Space.xs)
                    .width(dotWidth)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                    .bounceClick { onPageSelected(index) })
        }
    }
}

@Composable
private fun BottomControls(
    compact: Boolean,
    volume: Int?,
    muted: Boolean,
    onRemoteButtonClick: (TvKey) -> Unit,
    centerContent: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.Space.xs)
            .padding(horizontal = Dimens.Space.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm),
    ) {
        VerticalControl(
            iconTop = Icons.Default.Add,
            middle = "VOL",
            iconBottom = Icons.Default.Remove,
            level = volume?.let { it / 100f },
            waterColor = if (muted) colors.error.copy(alpha = 0.45f)
            else colors.primary.copy(alpha = 0.35f),
            modifier = Modifier
                .weight(0.9f)
                .height(if (compact) 160.dp else 180.dp),
            onTop = { onRemoteButtonClick(TvKey.VOLUME_UP) },
            onBottom = { onRemoteButtonClick(TvKey.VOLUME_DOWN) },
        )
        Column(
            modifier = Modifier.weight(3f),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.sm),
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
    val colors = MaterialTheme.colorScheme

    RoundControl(
        icon = IconSource.Resource(if (isMuted) R.drawable.ic_mute else R.drawable.ic_unmute),
        description = if (isMuted) "Unmute" else "Mute",
        modifier = modifier,
        containerColor = if (isMuted) colors.errorContainer else colors.surfaceContainer,
        iconTint = if (isMuted) colors.error else colors.onSurface,
        onClick = onClick,
    )
}

@Composable
internal fun ColumnScope.RemoteControls(
    uiState: RemoteUiState,
    pagerState: PagerState,
    compact: Boolean,
    onRemoteButtonClick: (TvKey) -> Unit,
    touchpadMode: TouchpadMode,
    touchpadEvent: (TouchpadEvent) -> Unit = {},
    onPageSelected: (Int) -> Unit,
    showKeyboardSheet: () -> Unit,
    onStartVoiceRecording: () -> Unit,
    onStopVoiceRecording: () -> Unit,
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

    PageDots(
        selectedPage = uiState.selectedPage,
        onPageSelected = onPageSelected,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

    BottomControls(
        compact = compact,
        onRemoteButtonClick = onRemoteButtonClick,
        volume = uiState.volume?.level,
        muted = uiState.volume?.muted == true,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm)) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm)) {
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

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm)) {
            val muted = uiState.volume?.muted == true
            MuteControl(
                isMuted = muted,
                modifier = Modifier.weight(1f),
                onClick = { onRemoteButtonClick(TvKey.MUTE) },
            )
            if (uiState.supportsVoice) {
                if (!uiState.isRecording) {
                    RoundControl(
                        icon = IconSource.Vector(Icons.Default.Mic),
                        description = "Voice",
                        modifier = Modifier.weight(1f),
                    ) {
                        onStartVoiceRecording()
                    }
                } else {
                    RecordingControl(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onStopVoiceRecording()
                        }
                    )
                }
            }
            RoundControl(
                icon = IconSource.Vector(Icons.AutoMirrored.Filled.KeyboardBackspace),
                description = "Back",
                modifier = Modifier.weight(1f),
            ) { onRemoteButtonClick(TvKey.BACK) }
        }
    }
}

@Composable
private fun DPad(
    diameter: Dp,
    onRemoteButtonClick: (TvKey) -> Unit,
) {
    val ringPadding = Dimens.Space.screenHorizontal
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
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    width = RingStroke,
                    brush = Brush.sweepGradient(RingColors),
                    shape = CircleShape,
                )
                .bounceClick { onRemoteButtonClick(TvKey.OK) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "OK",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
        verticalArrangement = Arrangement.spacedBy(Dimens.Space.sm),
    ) {
        numberPadRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space.sm),
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
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (isDelete) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = shape,
                    )
                } else {
                    Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = shape,
                        )
                        .border(Dimens.Border.thin, hairline(), shape)
                }
            )
            .repeatableClick(onClick = onClick, indication = ripple()),
        contentAlignment = Alignment.Center,
    ) {
        if (isDelete) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                modifier = Modifier.size(Dimens.Size.iconMedium + Dimens.Space.xxs),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        } else if (key.isNotEmpty()) {
            Text(
                text = key,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
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
            .repeatableClick(
                indication = ripple(),
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
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = iconPadding.size(Dimens.Size.iconLarge),
        )
    }
}

@Composable
private fun RoundControl(
    modifier: Modifier = Modifier,
    icon: IconSource,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    description: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    onClick: () -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .widthIn(max = 80.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .bounceClick(shape = CircleShape, onClick = onClick)
                .background(containerColor)
                .border(Dimens.Border.thin, hairline(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when (icon) {
                is IconSource.Vector -> {
                    Icon(
                        imageVector = icon.imageVector,
                        contentDescription = description,
                        tint = iconTint,
                        modifier = Modifier.size(Dimens.Size.iconLarge)
                    )
                }

                is IconSource.Resource -> {
                    Icon(
                        painter = painterResource(id = icon.resId),
                        contentDescription = description,
                        tint = iconTint,
                        modifier = Modifier.size(Dimens.Size.iconLarge)
                    )
                }
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
    level: Float? = null,
    waterColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
    onTop: () -> Unit,
    onBottom: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(Dimens.Border.thin, hairline(), CircleShape),
    ) {
        if (level != null) {
            WaterFill(
                level = level,
                color = waterColor,
                modifier = Modifier.matchParentSize(),
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            Box(
                Modifier
                    .bounceClickRepeatable(onClick = onTop, shape = CircleShape, debounceMs = 0)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconTop,
                    contentDescription = "IconTop$middle",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(Dimens.Size.iconLarge),
                )
            }
            Text(
                text = middle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Box(
                Modifier
                    .bounceClickRepeatable(shape = CircleShape, onClick = onBottom, debounceMs = 0)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconBottom,
                    contentDescription = "IconBottom$middle",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(Dimens.Size.iconLarge),
                )
            }
        }
    }
}

@Composable
private fun WaterFill(
    level: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 300),
        label = "waterColor",
    )

    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "waterLevel",
    )

    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
        ),
        label = "wavePhase",
    )

    Canvas(modifier = modifier) {
        val fill = animatedLevel.coerceIn(0f, 1f)
        if (fill <= 0f) return@Canvas

        val waterTop = size.height * (1f - fill)
        val amplitude = 4.dp.toPx() * minOf(fill, 1f - fill).coerceAtMost(0.1f) / 0.1f
        val step = 4f

        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, waterTop)
            var x = 0f
            while (x <= size.width) {
                val y = waterTop + amplitude * sin(x / size.width * 2f * PI.toFloat() + phase)
                lineTo(x, y)
                x += step
            }
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path = path, color = animatedColor)
    }
}

@Composable
private fun RecordingControl(
    modifier: Modifier = Modifier,
    description: String = "Stop recording",
    activeColor: Color = MaterialTheme.colorScheme.error,
    onActiveColor: Color = MaterialTheme.colorScheme.onError,
    onClick: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "recording")

    val wave1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val wave2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(700)
        ),
        label = "wave2"
    )

    val micScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .drawBehind {
                val baseRadius = size.minDimension / 2f
                listOf(wave1, wave2).forEach { progress ->
                    drawCircle(
                        color = activeColor.copy(alpha = (1f - progress) * 0.35f),
                        radius = baseRadius * (1f + progress * 0.6f)
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .bounceClick(shape = CircleShape, onClick = onClick)
                .background(activeColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = description,
                tint = onActiveColor,
                modifier = Modifier
                    .size(Dimens.Size.iconLarge)
                    .scale(micScale)
            )
        }
    }

}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun RemoteControlsPreview() {
    RemoteLASTvTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val pagerState = rememberPagerState(pageCount = { 2 })
            Column(Modifier.fillMaxSize()) {
                RemoteControls(
                    uiState = RemoteUiState(),
                    pagerState = pagerState,
                    compact = false,
                    onRemoteButtonClick = {},
                    touchpadMode = TouchpadMode.DIRECTIONAL,
                    onPageSelected = {},
                    showKeyboardSheet = {},
                    onStartVoiceRecording = {},
                    onStopVoiceRecording = {},
                )
            }
        }
    }
}
