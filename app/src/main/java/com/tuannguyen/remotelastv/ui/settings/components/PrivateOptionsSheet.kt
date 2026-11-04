package com.tuannguyen.remotelastv.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.common.IconSource
import com.tuannguyen.remotelastv.ui.theme.Dimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyOptionsSheet(
    onPrivacyPolicy: () -> Unit,
    onAdOptions: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    fun dismissThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            action()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(bottom = Dimens.Space.lg)) {
            SettingsItem(
                title = stringResource(R.string.privacy_policy),
                icon = IconSource.Vector(Icons.Default.Security),
                onClick = { dismissThen(onPrivacyPolicy) },
                showChevron = false,
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            HorizontalDivider(
                modifier = Modifier.padding(
                    start = Dimens.Space.md + SettingsIconTileSize + Dimens.Space.md,
                    end = Dimens.Space.md,
                ),
                thickness = Dimens.Border.thin,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            SettingsItem(
                title = stringResource(R.string.ad_privacy_options),
                icon = IconSource.Vector(Icons.Default.AdsClick),
                onClick = { dismissThen(onAdOptions) },
                showChevron = false,
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}