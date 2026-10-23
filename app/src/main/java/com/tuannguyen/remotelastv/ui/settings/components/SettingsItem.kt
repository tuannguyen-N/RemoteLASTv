package com.tuannguyen.remotelastv.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.ui.common.IconSource
import com.tuannguyen.remotelastv.ui.common.safeClickable
import com.tuannguyen.remotelastv.ui.theme.Dimens

/** 40dp tile behind the leading icon. Derived from Dimens so it stays on the spacing scale. */
internal val SettingsIconTileSize = Dimens.Size.minTouchTarget - Dimens.Space.xs

@Composable
fun SettingsItem(
    title: String,
    icon: IconSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true,
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.Size.minTouchTarget)
            .safeClickable(onClick = onClick)
            .padding(horizontal = Dimens.Space.md, vertical = Dimens.Space.xs),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(SettingsIconTileSize)
                .background(iconContainerColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            SettingsIcon(source = icon, tint = iconColor)
        }

        Spacer(Modifier.width(Dimens.Space.md))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        // Chevron only for rows that open another screen; Share / Rate / Feedback are actions.
        if (showChevron) {
            Spacer(Modifier.width(Dimens.Space.sm))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.Size.iconMedium),
            )
        }
    }
}

@Composable
private fun SettingsIcon(source: IconSource, tint: Color) {
    // Decorative: the row's text already describes it, so TalkBack doesn't read the icon twice.
    when (source) {
        is IconSource.Vector -> Icon(
            imageVector = source.imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Dimens.Size.iconMedium),
        )

        is IconSource.Resource -> Icon(
            painter = painterResource(id = source.resId),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Dimens.Size.iconMedium),
        )
    }
}