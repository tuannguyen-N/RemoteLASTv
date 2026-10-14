package com.tuannguyen.remotelastv.ui.settings_connection_display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.components.CustomSwitch
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDisplayContent(
    uiState: ConnectionDisplayUiState,
    onNavigateBack: () -> Unit,
    onAutoReconnectChanged: (Boolean) -> Unit,
    onKeepScreenAwakeChanged: (Boolean) -> Unit,
    onTheaterModeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.connection_display))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding)
                .padding(bottom = Dimens.Space.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.md)
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            PreferenceRow(
                title = stringResource(R.string.auto_reconnect),
                description = stringResource(R.string.auto_reconnect_description),
                checked = uiState.isAutoReconnectEnabled,
                onCheckedChange = onAutoReconnectChanged
            )

            PreferenceRow(
                title = stringResource(R.string.keep_screen_awake),
                description = stringResource(R.string.keep_screen_awake_description),
                checked = uiState.isKeepScreenAwakeEnabled,
                onCheckedChange = onKeepScreenAwakeChanged
            )

            PreferenceRow(
                title = stringResource(R.string.theater_mode),
                description = stringResource(R.string.theater_mode_description),
                checked = uiState.isTheaterModeEnabled,
                onCheckedChange = onTheaterModeChanged
            )
        }
    }
}

@Composable
private fun PreferenceRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(Dimens.Space.md))

        CustomSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionDisplayContentPreview() {
    RemoteLASTvTheme {
        ConnectionDisplayContent(
            uiState = ConnectionDisplayUiState(),
            onNavigateBack = {},
            onAutoReconnectChanged = {},
            onKeepScreenAwakeChanged = {},
            onTheaterModeChanged = {}
        )
    }
}
