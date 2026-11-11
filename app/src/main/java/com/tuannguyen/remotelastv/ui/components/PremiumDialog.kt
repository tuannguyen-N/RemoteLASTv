package com.tuannguyen.remotelastv.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.common.IconSource
import com.tuannguyen.remotelastv.ui.theme.ButtonColors
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

@Composable
fun PremiumDialog(
    onDismiss: () -> Unit,
    onGoPremium: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        PremiumDialogContent(
            onDismiss = onDismiss,
            onGoPremium = onGoPremium,
            modifier = modifier,
        )
    }
}

@Composable
fun PremiumDialogContent(
    onDismiss: () -> Unit,
    onGoPremium: () -> Unit,
    modifier: Modifier = Modifier,
    heroIcon: IconSource = IconSource.Resource(R.drawable.ic_tv_no_ads),
    bannerIcon: IconSource = IconSource.Resource(R.drawable.ic_no_ads),
    boltIcon: IconSource = IconSource.Vector(Icons.Filled.Bolt),
    starIcon: IconSource = IconSource.Vector(Icons.Filled.Star),
    crownIcon: IconSource = IconSource.Vector(Icons.Filled.WorkspacePremium),
    closeIcon: IconSource = IconSource.Vector(Icons.Filled.Close),
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppIcon(
                source = heroIcon,
                modifier = Modifier.size(width = 220.dp, height = 150.dp),
            )


            Text(
                text = stringResource(R.string.premium_dialog_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.premium_dialog_description),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(source = bannerIcon, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.premium_feature_no_ads_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.premium_feature_no_ads_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            FeatureRow(
                icon = boltIcon,
                text = stringResource(R.string.premium_dialog_feature_better_faster),
            )
            Spacer(Modifier.height(12.dp))
            FeatureRow(
                icon = starIcon,
                text = stringResource(R.string.premium_feature_support_title),
            )

            Spacer(Modifier.height(24.dp))

            GradientButton(
                text = stringResource(R.string.premium_dialog_go_premium),
                icon = crownIcon,
                onClick = onGoPremium,
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(CircleShape)
                    .border(1.dp, colors.outline, CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.premium_dialog_maybe_later),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surfaceContainerHighest)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(
                source = closeIcon,
                contentDescription = stringResource(R.string.premium_close),
                tint = colors.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun AppIcon(
    source: IconSource,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
) {
    when (source) {
        is IconSource.Vector -> Icon(
            imageVector = source.imageVector,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint,
        )

        is IconSource.Resource -> Image(
            painter = painterResource(source.resId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun FeatureRow(icon: IconSource, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(48.dp), contentAlignment = Alignment.Center) {
            AppIcon(
                source = icon,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun GradientButton(
    text: String,
    icon: IconSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    val content = Color.White
    val backgroundModifier = if (color != null) {
        Modifier.background(color)
    } else {
        Modifier.background(Brush.horizontalGradient(ButtonColors))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .then(backgroundModifier)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        AppIcon(source = icon, tint = content, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
    }
}

@Preview(
    name = "Premium dialog",
    showBackground = true,
)
@Composable
private fun PremiumDialogContentPreview() {
    RemoteLASTvTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            PremiumDialogContent(
                onDismiss = {},
                onGoPremium = {},
            )
        }
    }
}