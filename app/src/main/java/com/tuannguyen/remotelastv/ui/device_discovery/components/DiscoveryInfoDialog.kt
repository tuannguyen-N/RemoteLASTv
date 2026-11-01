package com.tuannguyen.remotelastv.ui.device_discovery.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.ui.theme.Dimens

@Composable
fun DiscoveryInfoDialog(
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
        },
        title = {
            Text(stringResource(R.string.discovery_info_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space.md)) {
                Text(
                    text = stringResource(R.string.discovery_info_scan_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = stringResource(R.string.discovery_info_supported_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                SupportedBrandRow(
                    brand = TelevisionBrand.LG,
                    name = stringResource(R.string.brand_lg),
                )
                SupportedBrandRow(
                    brand = TelevisionBrand.ANDROID_TV,
                    name = stringResource(R.string.brand_android_tv),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.discovery_info_got_it))
            }
        },
    )
}

@Composable
private fun SupportedBrandRow(
    brand: TelevisionBrand,
    name: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TelevisionBrandLogo(
            brand = brand,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(Dimens.Space.md))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun TelevisionBrandLogo(
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