package com.tuannguyen.remotelastv.ui.device_discovery.components

import androidx.compose.foundation.Image
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.model.DiscoveryStatus
import com.tuannguyen.remotelastv.domain.model.FailReason
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.ui.components.DialogLoading
import com.tuannguyen.remotelastv.ui.device_discovery.DeviceDiscoveryUiState
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import com.tuannguyen.remotelastv.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDiscoveryContent(
    modifier: Modifier = Modifier,
    uiState: DeviceDiscoveryUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onTelevisionClick: (Television) -> Unit,
    onDisconnectClick: (Television) -> Unit,
    onDismissFailure: () -> Unit = {},
    onReturnHome: () -> Unit,
    successTvName: String? = null,
    onSubmitPin: (String) -> Unit = {},
    onCancelSendPin: () -> Unit = {},
) {
    val connectedTv = (uiState.connectionState as? ConnectionState.Connected)?.tv

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
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
                when (val status = uiState.discoveryStatus) {
                    is DiscoveryStatus.Scanning -> {
                        DiscoveryContent(
                            message = stringResource(
                                R.string.televisions_discovery_message,
                            ),
                            modifier = Modifier.weight(1f),
                            showLoading = true,
                            televisions = uiState.televisions,
                            onTelevisionClick = onTelevisionClick,
                            onDisconnectClick = onDisconnectClick,
                            connectedTv = connectedTv,
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
                            connectedTv = connectedTv
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
                                    connectedTv = connectedTv,
                                    onTelevisionClick = onTelevisionClick,
                                    onDisconnectClick = onDisconnectClick,
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
                                        text = stringResource(R.string.discovery_rescan),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        when (uiState.connectionState) {
            is ConnectionState.Connecting -> {
                DialogLoading()
            }

            is ConnectionState.WaitingForPin -> {
                DialogPinEnter(
                    onSubmit = { onSubmitPin(it) },
                    onCancel = { onCancelSendPin() }
                )
            }

            is ConnectionState.Failed -> {
                when (uiState.connectionState.reason) {
                    FailReason.DEVICE_NOT_FOUND -> {
                        DialogConnectionFailure(
                            title = stringResource(R.string.failure_device_not_found_title),
                            errorMessage = stringResource(R.string.failure_device_not_found_message),
                            onRetry = onDismissFailure
                        )
                    }

                    FailReason.TIMEOUT -> {
                        DialogConnectionFailure(
                            title = stringResource(R.string.failure_connection_timeout_title),
                            errorMessage = stringResource(R.string.failure_connection_timeout_message),
                            onRetry = onDismissFailure
                        )
                    }

                    FailReason.PAIRING_REJECTED -> {
                        DialogConnectionFailure(
                            title = stringResource(R.string.failure_wrong_pin_title),
                            errorMessage = stringResource(R.string.failure_wrong_pin_message),
                            onRetry = onDismissFailure
                        )
                    }

                    FailReason.PAIRING_DECLINED -> {
                        DialogConnectionFailure(
                            title = stringResource(R.string.failure_pairing_rejected_title),
                            errorMessage = stringResource(R.string.failure_pairing_rejected_message),
                            onRetry = onDismissFailure
                        )
                    }

                    FailReason.UNSUPPORTED_BRAND -> {
                        DialogConnectionFailure(
                            title = stringResource(R.string.failure_unsupported_tv_title),
                            errorMessage = stringResource(R.string.failure_unsupported_tv_message),
                            onRetry = onDismissFailure
                        )
                    }

                    FailReason.UNKNOWN -> {
                        DialogConnectionFailure(
                            title = stringResource(R.string.failure_connection_failed_title),
                            errorMessage = stringResource(R.string.failure_connection_failed_message),
                            onRetry = onDismissFailure
                        )
                    }

                    FailReason.NETWORK_ERROR -> {
                        DialogConnectionFailure(
                            title = stringResource(R.string.failure_network_error_title),
                            errorMessage = stringResource(R.string.failure_network_error_message),
                            onRetry = onDismissFailure
                        )
                    }
                }
            }

            else -> Unit
        }
    }

    successTvName?.let { tvName ->
        DialogConnectionSuccess(
            televisionName = tvName,
            onContinue = onReturnHome
        )
    }

}

@Composable
private fun DiscoveryContent(
    message: String,
    modifier: Modifier = Modifier,
    showLoading: Boolean,
    televisions: List<Television> = emptyList(),
    onTelevisionClick: (Television) -> Unit = {},
    onDisconnectClick: (Television) -> Unit = {},
    connectedTv: Television? = null,
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
                onDisconnectClick = onDisconnectClick,
                connectedTv = connectedTv
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
                text = stringResource(R.string.no_tvs_found),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(Dimens.Space.sm))

            Text(
                text = stringResource(R.string.make_sure_your_tv_is_turned_on_and_connected_to_the_same_wi_fi_network_as_your_phone),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimens.Space.xl))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.common_try_again))
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
    connectedTv: Television?,
    onTelevisionClick: (Television) -> Unit,
    onDisconnectClick: (Television) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space.md),
        contentPadding = PaddingValues(bottom = Dimens.Space.md),
    ) {
        items(
            items = televisions,
            key = { television -> television.ipAddress },
        ) { television ->
            val isConnected = television.ipAddress == connectedTv?.ipAddress

            TelevisionItem(
                television = television,
                isConnected = isConnected,
                onConnectClick = {
                    onTelevisionClick(television)
                },
                onDisconnectClick = {
                    onDisconnectClick(television)
                },
            )
        }
    }
}

@Composable
private fun TelevisionItem(
    television: Television,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonColor = if (isConnected) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    val buttonText = if (isConnected) {
        stringResource(R.string.televisions_disconnect_action)
    } else {
        stringResource(R.string.televisions_connect_action)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Space.md),
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
                if (isConnected) {
                    ConnectedBanner()
                }

                Text(
                    text = television.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (!isConnected) {
                    Spacer(modifier = Modifier.height(Dimens.Space.xxs))
                    Text(
                        text = stringResource(
                            R.string.televisions_available_label,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TextButton(
                onClick = {
                    if (isConnected) {
                        onDisconnectClick()
                    } else {
                        onConnectClick()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = buttonColor,
                ),
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ConnectedBanner() {
    Text(
        text = stringResource(R.string.discovery_connecting),
        modifier = Modifier.padding(
            vertical = Dimens.Space.xxs,
        ),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.extendedColors.success,
    )
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
            onReturnHome = {},
//            successTvName = null,
            onDisconnectClick = {},
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
                        id = "tv-1",
                        ipAddress = "tv-1",
                        name = "Living Room TV",
                    ), Television(
                        id = "tv-2",
                        ipAddress = "tv-2",
                        name = "Bedroom TV",
                    ), Television(
                        id = "tv-3",
                        ipAddress = "tv-3",
                        name = "Living Room TV",
                        brand = TelevisionBrand.SAMSUNG,
                    )
                ),
                connectionState = ConnectionState.Connected(
                    Television(
                        id = "tv-1",
                        ipAddress = "tv-1",
                        name = "Living Room TV",
                        brand = TelevisionBrand.ANDROID_TV,
                    )
                ),
            ),
            onClose = {},
            onRetry = {},
            onTelevisionClick = {},
            onReturnHome = {},
            onDisconnectClick = {},
//            successTvName = null,
        )
    }
}

@Preview(showBackground = true, name = "Scanning - TV Success")
@Composable
private fun DeviceDiscoverySuccessWithDevicesPreview() {
    RemoteLASTvTheme {
        DeviceDiscoveryContent(
            uiState = DeviceDiscoveryUiState(
                discoveryStatus = DiscoveryStatus.Completed,
                televisions = listOf(
                    Television(
                        id = "tv-1",
                        ipAddress = "tv-1",
                        name = "Living Room TV",
                    ), Television(
                        id = "tv-2",
                        ipAddress = "tv-2",
                        name = "Bedroom TV",
                    ), Television(
                        id = "tv-3",
                        ipAddress = "tv-3",
                        name = "Living Room TV",
                        brand = TelevisionBrand.SAMSUNG,
                    )
                ),
            ),
            onClose = {},
            onRetry = {},
            onTelevisionClick = {},
            onReturnHome = {},
            onDisconnectClick = {},
//            successTvName = "",
        )
    }
}