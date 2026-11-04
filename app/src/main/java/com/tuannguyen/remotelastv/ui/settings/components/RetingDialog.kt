package com.tuannguyen.remotelastv.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.theme.ButtonColors
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

private val StarColor = Color(0xFFFFD166)
private const val MAX_STARS = 5

@Composable
fun RatingDialog(
    onRateClick: (rating: Int) -> Unit,
//    onLaterClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        RatingDialogContent(
            onRateClick = onRateClick,
//            onLaterClick = onLaterClick,
            modifier = Modifier.padding(horizontal = Dimens.Space.screenHorizontal),
        )
    }
}

@Composable
fun RatingDialogContent(
    onRateClick: (rating: Int) -> Unit,
//    onLaterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var rating by rememberSaveable { mutableIntStateOf(0) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.Radius.card),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(Dimens.Size.dialogIconContainer)
                    .clip(RoundedCornerShape(Dimens.Radius.card))
                    .background(ButtonColors[0].copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = ButtonColors[2],
                    modifier = Modifier.size(Dimens.Size.iconLarge),
                )
            }

            Text(
                text = stringResource(R.string.rating_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Dimens.Space.md),
            )
            Text(
                text = stringResource(R.string.rating_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Dimens.Space.xs),
            )

            Row(
                modifier = Modifier.padding(vertical = Dimens.Space.lg),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space.xs),
            ) {
                repeat(MAX_STARS) { index ->
                    Icon(
                        imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = StarColor,
                        modifier = Modifier.size(Dimens.Size.iconLarge)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) { rating = index + 1 },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.Size.dialogButtonHeight)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(ButtonColors))
                    .clickable(role = Role.Button) { onRateClick(rating) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.rating_rate_on_google_play),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }

//            Box(
//                modifier = Modifier
//                    .padding(top = Dimens.Space.xs)
//                    .fillMaxWidth()
//                    .height(Dimens.Size.dialogButtonHeight)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.surfaceVariant)
//                    .clickable(role = Role.Button, onClick = onLaterClick),
//                contentAlignment = Alignment.Center,
//            ) {
//                Text(
//                    text = stringResource(R.string.rating_maybe_later),
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onSurface,
//                )
//            }

            Text(
                text = stringResource(R.string.rating_thank_you),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.Space.md),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun RatingDialogContentPreview() {
    RemoteLASTvTheme {
        RatingDialogContent(
            onRateClick = {},
            modifier = Modifier.padding(Dimens.Space.screenHorizontal),
        )
    }
}