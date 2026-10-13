package com.tuannguyen.remotelastv.ui.device_discovery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.TvOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.DiscoveryStatus
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDiscoveryContent(
    uiState: DeviceDiscoveryUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onTelevisionClick: (Television) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = {
                Text(stringResource(R.string.scan_televisions))
            }, navigationIcon = {
                IconButton(
                    onClick = onClose
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(
                            R.string.settings_back
                        )
                    )
                }
            })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding),
        ) {
            when (val status = uiState.status) {
                is DiscoveryStatus.Scanning -> {
                    DiscoveryContent(
                        message = stringResource(
                            R.string.televisions_discovery_message,
                        ),
                        modifier = Modifier.weight(1f),
                        showLoading = true,
                        televisions = uiState.televisions,
                        onTelevisionClick = onTelevisionClick,
                    )
                }

                is DiscoveryStatus.Error -> {
                    DiscoveryContent(
                        message = status.message,
                        modifier = Modifier.weight(1f),
                        showLoading = false,
                        action = {
                            Button(onClick = onRetry) {
                                Text(
                                    text = stringResource(
                                        R.string.televisions_retry
                                    )
                                )
                            }
                        },
                    )
                }

                is DiscoveryStatus.Completed -> {
                    if (uiState.isEmpty) {
                        DiscoveryEmptyContent(
                            onRetry = onRetry,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.height(Dimens.Space.md))

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            TelevisionList(
                                televisions = uiState.televisions,
                                onTelevisionClick = onTelevisionClick,
                                modifier = Modifier.weight(1f),
                            )

                            Button(
                                onClick = onRetry,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = Dimens.Space.md,
                                        horizontal = Dimens.Space.xxl
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(
                                        R.string.settings_back
                                    ),
                                    modifier = Modifier.padding(end = Dimens.Space.xs),
                                )

                                Text(
                                    text = "Rescan",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryContent(
    message: String,
    modifier: Modifier = Modifier,
    showLoading: Boolean,
    televisions: List<Television> = emptyList(),
    onTelevisionClick: (Television) -> Unit = {},
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showLoading) {
            DiscoveryLoadingIndicator(
                modifier = Modifier.padding(
                    vertical = Dimens.Space.lg,
                ),
            )
        }

        if (televisions.isEmpty()) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            TelevisionList(
                televisions = televisions,
                onTelevisionClick = onTelevisionClick,
                modifier = Modifier.weight(1f),
            )
        }

        if (action != null) {
            Spacer(
                modifier = Modifier.height(Dimens.Space.md),
            )
            action()
        }
    }
}


@Composable
private fun DiscoveryEmptyContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Space.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.TvOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimens.Space.lg))

            Text(
                text = "No TVs found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(Dimens.Space.sm))

            Text(
                text = "Make sure your TV is turned on and connected to the same Wi-Fi network as your phone.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimens.Space.xl))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Try Again")
            }
        }
    }
}


@Composable
private fun DiscoveryLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scanning))

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier.size(150.dp),
    )
}

@Composable
private fun TelevisionList(
    televisions: List<Television>,
    onTelevisionClick: (Television) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space.md),
        contentPadding = PaddingValues(bottom = Dimens.Space.md)
    ) {
        items(
            items = televisions,
            key = { television -> television.ipAddress },
        ) { television ->
            TelevisionItem(
                television = television,
                onClick = { onTelevisionClick(television) },
            )
        }
    }
}

@Composable
private fun TelevisionItem(
    television: Television,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TelevisionBrandLogo(
                brand = television.brand,
                modifier = Modifier.size(40.dp),
            )

            Spacer(modifier = Modifier.width(Dimens.Space.md))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = television.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(Dimens.Space.xs))

                Text(
                    text = stringResource(
                        R.string.televisions_available_label
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        R.string.televisions_connect_action
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TelevisionBrandLogo(
    brand: TelevisionBrand,
    modifier: Modifier = Modifier,
) {
    val logoRes = when (brand) {
        TelevisionBrand.ANDROID_TV -> R.drawable.img_logo_android_tv
        TelevisionBrand.LG -> R.drawable.img_logo_lg
        TelevisionBrand.SAMSUNG -> R.drawable.img_logo_samsung
        TelevisionBrand.UNKNOWN -> null
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        if (logoRes != null) {
            Image(
                painter = painterResource(logoRes),
                contentDescription = stringResource(R.string.tv_brand_logo),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "Scanning - No TV Found")
@Composable
private fun DeviceDiscoveryScanningPreview() {
    RemoteLASTvTheme {
        DeviceDiscoveryContent(
            uiState = DeviceDiscoveryUiState(DiscoveryStatus.Scanning),
            onClose = {},
            onRetry = {},
            onTelevisionClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Scanning - TV Found")
@Composable
private fun DeviceDiscoveryScanningWithDevicesPreview() {
    RemoteLASTvTheme {
        DeviceDiscoveryContent(
            uiState = DeviceDiscoveryUiState(
                DiscoveryStatus.Scanning,
                televisions = listOf(
                    Television(
                        ipAddress = "tv-1",
                        name = "Living Room TV",
                    ), Television(
                        ipAddress = "tv-2",
                        name = "Bedroom TV",
                    ), Television(
                        ipAddress = "tv-3",
                        name = "Living Room TV",
                        brand = TelevisionBrand.SAMSUNG,
                    ), Television(
                        ipAddress = "tv-4",
                        name = "Bedroom TV",
                        brand = TelevisionBrand.ANDROID_TV,
                    ), Television(
                        ipAddress = "tv-5",
                        name = "Living Room TV",
                        brand = TelevisionBrand.LG,
                    ), Television(
                        ipAddress = "tv-61",
                        name = "Bedroom TV",
                        brand = TelevisionBrand.ANDROID_TV,
                    ), Television(
                        ipAddress = "tv-6",
                        name = "Living Room TV",
                        brand = TelevisionBrand.SAMSUNG,
                    ), Television(
                        ipAddress = "tv-7",
                        name = "Bedroom TV",
                        brand = TelevisionBrand.ANDROID_TV,
                    ), Television(
                        ipAddress = "tv-8",
                        name = "Living Room TV",
                        brand = TelevisionBrand.SAMSUNG,
                    ), Television(
                        ipAddress = "tv-9",
                        name = "Bedroom TV",
                        brand = TelevisionBrand.ANDROID_TV,
                    )
                )
            ),
            onClose = {},
            onRetry = {},
            onTelevisionClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Scanning - TV Success")
@Composable
private fun DeviceDiscoverySuccessWithDevicesPreview() {
    RemoteLASTvTheme {
        DeviceDiscoveryContent(
            uiState = DeviceDiscoveryUiState(
                status = DiscoveryStatus.Completed,
                televisions = listOf(
                    Television(
                        ipAddress = "tv-1",
                        name = "Living Room TV",
                    ), Television(
                        ipAddress = "tv-2",
                        name = "Bedroom TV",
                    ), Television(
                        ipAddress = "tv-3",
                        name = "Living Room TV",
                        brand = TelevisionBrand.SAMSUNG,
                    ), Television(
                        ipAddress = "tv-4",
                        name = "Bedroom TV",
                        brand = TelevisionBrand.ANDROID_TV,
                    ), Television(
                        ipAddress = "tv-5",
                        name = "Living Room TV",
                        brand = TelevisionBrand.SAMSUNG,
                    ), Television(
                        ipAddress = "tv-61",
                        name = "Bedroom TV",
                        brand = TelevisionBrand.ANDROID_TV,
                    ), Television(
                        ipAddress = "tv-6",
                        name = "Living Room TV",
                        brand = TelevisionBrand.SAMSUNG,
                    ), Television(
                        ipAddress = "tv-7",
                        name = "Bedroom TV",
                        brand = TelevisionBrand.ANDROID_TV,
                    ), Television(
                        ipAddress = "tv-8",
                        name = "Living Room TV",
                        brand = TelevisionBrand.SAMSUNG,
                    ), Television(
                        ipAddress = "tv-9",
                        name = "Bedroom TV",
                        brand = TelevisionBrand.ANDROID_TV,
                    )
                ),
            ),
            onClose = {},
            onRetry = {},
            onTelevisionClick = {},
        )
    }
}