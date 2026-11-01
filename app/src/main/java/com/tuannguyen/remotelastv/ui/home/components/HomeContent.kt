package com.tuannguyen.remotelastv.ui.home.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.ui.common.attentionPulse
import com.tuannguyen.remotelastv.ui.common.bounceClickNoEffect
import com.tuannguyen.remotelastv.ui.common.gradientTint
import com.tuannguyen.remotelastv.ui.common.logoRes
import com.tuannguyen.remotelastv.ui.common.safeClickableNoEffect
import com.tuannguyen.remotelastv.ui.home.HomeUiState
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import com.tuannguyen.remotelastv.ui.theme.RingColors
import com.tuannguyen.remotelastv.ui.theme.extendedColors
import kotlinx.coroutines.launch
private val HeaderHeight = 80.dp
private val HeaderHeightCompact = 64.dp
private val AppIconSize = 80.dp

@Composable
private fun hairline(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onSettingsClick: () -> Unit,
    onChangeTelevision: () -> Unit,
    onPowerClick: () -> Unit,
    onAppClick: (KnownApp) -> Unit,
    remotePage: @Composable () -> Unit,
    castPage: @Composable () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val screenPagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var showAllApps by remember { mutableStateOf(false) }

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
                    onChangeTelevision = onChangeTelevision,
                    onPowerClick = onPowerClick,
                    isCastPage = screenPagerState.currentPage == 1,
                    onCastClick = {
                        coroutineScope.launch {
                            screenPagerState.animateScrollToPage(
                                if (screenPagerState.currentPage == 0) 1 else 0
                            )
                        }
                    },
                )

                AppRow(
                    apps = uiState.shortcutApps,
                    onMoreClick = { showAllApps = true },
                    onAppClick = onAppClick,
                )

                HorizontalPager(
                    state = screenPagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    userScrollEnabled = false,
                ) { page ->
                    when (page) {
                        0 -> remotePage()
                        1 -> castPage()
                    }
                }
            }
        }
    }

    if (showAllApps) {
        MoreAppsDialog(
            onDismiss = { showAllApps = false },
            onAppClick = onAppClick,
            apps = uiState.availableApps,
        )
    }
}

@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    compact: Boolean,
    isCastPage: Boolean,
    onCastClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onChangeTelevision: () -> Unit,
    onPowerClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) HeaderHeightCompact else HeaderHeight)
            .padding(horizontal = Dimens.Space.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {

        HeaderIcon(
            iconRes = R.drawable.ic_on_off,
            description = "Power",
            onClick = onPowerClick,
            tint = MaterialTheme.colorScheme.error,
            containerColor = MaterialTheme.colorScheme.errorContainer,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.Space.sm),
            contentAlignment = Alignment.Center,
        ) {
            TvSelector(
                connectionState = uiState.connectionState,
                compact = compact,
                onClick = onChangeTelevision,
            )
        }

        HeaderIcon(
            iconRes = R.drawable.ic_cast,
            imageVector = if (isCastPage) Icons.Filled.SettingsRemote else null,
            description = if (isCastPage) "Remote" else "Cast",
            onClick = onCastClick,
            tintBrush = if (isCastPage) null else Brush.linearGradient(RingColors),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )

        Spacer(Modifier.width(Dimens.Space.xxs))

        HeaderIcon(
            iconRes = R.drawable.ic_setting,
            description = "Settings",
            onClick = onSettingsClick,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
private fun TvSelector(
    connectionState: ConnectionState,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val connected = connectionState as? ConnectionState.Connected
    val needsAttention = connected == null
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .attentionPulse(enabled = needsAttention, color = primary)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = Dimens.Border.thin,
                color = if (needsAttention) primary else hairline(),
                shape = CircleShape,
            )
            .safeClickableNoEffect(onClick)
            .padding(
                start = Dimens.Space.md,
                end = Dimens.Space.xs,
                top = Dimens.Space.xs,
                bottom = Dimens.Space.xs,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (connected != null) {
            StatusDot()
            Spacer(Modifier.width(Dimens.Space.xs))
        }

        Text(
            text = connected?.tv?.name ?: stringResource(R.string.choose_a_tv),
            modifier = Modifier.weight(1f, fill = false),
            color = if (needsAttention) primary else MaterialTheme.colorScheme.onSurface,
            fontSize = if (compact) 16.sp else 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = stringResource(R.string.choose_a_tv),
            modifier = Modifier.size(Dimens.Size.iconMedium),
            tint = if (needsAttention) primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusDot() {
    val success = MaterialTheme.extendedColors.success
    Box(
        modifier = Modifier
            .size(Dimens.Space.sm)
            .clip(CircleShape)
            .background(success.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.Space.xs)
                .clip(CircleShape)
                .background(success),
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
private fun AppRow(
    apps: List<KnownApp>,
    onAppClick: (KnownApp) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimens.Space.lg),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space.md),
    ) {
        items(
            items = apps, key = { it },
        ) { app ->
            AppLauncher(
                app = app,
                onClick = onAppClick,
            )
        }

        item(key = "more_apps") {
            MoreAppsLauncher(
                onClick = onMoreClick,
            )
        }
    }
}

@Composable
private fun AppLauncher(
    app: KnownApp, onClick: (KnownApp) -> Unit, modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    Image(
        painter = painterResource(app.logoRes()),
        contentDescription = app.name,
        modifier = modifier
            .size(AppIconSize)
            .bounceClickNoEffect(onClick = { onClick(app) })
            .clip(shape),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun MoreAppsLauncher(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .size(AppIconSize)
            .bounceClickNoEffect(onClick = onClick)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(Dimens.Border.thin, hairline(), shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.MoreHoriz,
            contentDescription = stringResource(R.string.home_more_apps),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.Size.iconLarge),
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeContentPreview() {
    RemoteLASTvTheme {
        HomeContent(
            uiState = HomeUiState(),
            onSettingsClick = {},
            onChangeTelevision = {},
            onPowerClick = {},
            onAppClick = {},
            remotePage = { Text("Remote page") },
            castPage = { Text("Cast page") },
        )
    }
}
