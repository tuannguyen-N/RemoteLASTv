package com.tuannguyen.remotelastv.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.tuannguyen.remotelastv.ui.common.IconSource
import com.tuannguyen.remotelastv.ui.common.safeClickable
import com.tuannguyen.remotelastv.ui.theme.Dimens

@Composable
fun SettingsItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: IconSource = IconSource.Vector(Icons.AutoMirrored.Filled.KeyboardArrowRight),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.Size.minTouchTarget)
            .safeClickable(onClick = onClick)
            .padding(vertical = Dimens.Space.xs, horizontal = Dimens.screenPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(Dimens.Space.md))
        when (trailing) {
            is IconSource.Vector -> {
                androidx.compose.material3.Icon(
                    imageVector = trailing.imageVector,
                    contentDescription = title,
                    modifier = Modifier.size(Dimens.Size.iconMedium)
                )
            }

            is IconSource.Resource -> {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = trailing.resId),
                    contentDescription = title,
                    modifier = Modifier.size(Dimens.Size.iconMedium)
                )
            }
        }
    }
}