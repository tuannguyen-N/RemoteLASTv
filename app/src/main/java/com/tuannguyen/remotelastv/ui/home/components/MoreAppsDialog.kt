package com.tuannguyen.remotelastv.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.ui.common.logoRes
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

@Composable
fun MoreAppsDialog(
    apps: List<KnownApp>,
    onAppClick: (KnownApp) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        title = {
            Text(
                text = stringResource(R.string.home_available_apps),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.Space.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.home_no_apps_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Space.md),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space.lg),
                ) {
                    items(
                        items = apps,
                        key = { app -> app },
                    ) { app ->
                        AppGridItem(
                            app = app,
                            onClick = { onAppClick(app) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        },
    )
}

@Composable
private fun AppGridItem(
    app: KnownApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(Dimens.Space.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs),
    ) {
        Image(
            painter = painterResource(app.logoRes()),
            contentDescription = app.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(Dimens.Size.appLogoSize)
                .clip(MaterialTheme.shapes.small),
        )

        Text(
            text = app.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(
    name = "More Apps Dialog",
    showBackground = true,
)
@Composable
private fun MoreAppsDialogPreview() {
    RemoteLASTvTheme {
        MoreAppsDialog(
            apps = KnownApp.entries,
            onAppClick = {},
            onDismiss = {},
        )
    }
}