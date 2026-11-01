package com.tuannguyen.remotelastv.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.common.bounceClickNoEffect
import com.tuannguyen.remotelastv.ui.common.gradientTint
import com.tuannguyen.remotelastv.ui.theme.Dimens

@Composable
fun CastingToHeader(
    tvName: String,
    onStopCastingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Cast,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.Size.iconMedium),
        )

        Spacer(Modifier.width(Dimens.Space.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.cast_video_casting_to),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = tvName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(Dimens.Space.sm))

        HeaderIcon(
            iconRes = R.drawable.ic_casting_disconnect,
            description = stringResource(R.string.cast_video_stop),
            onClick = onStopCastingClick,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            tint = MaterialTheme.colorScheme.error
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
            .size(Dimens.Size.iconMedium)
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