package com.tuannguyen.remotelastv.ui.settings_remote_preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.settings_remote_preferences.components.PreferenceDivider
import com.tuannguyen.remotelastv.ui.settings_remote_preferences.components.PreferenceRow
import com.tuannguyen.remotelastv.ui.settings_remote_preferences.components.PreferenceSection
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemotePreferencesContent(
    preferencesUiState: RemotePreferencesUiState,
    onNavigateBack: () -> Unit,
    onAutoReconnectChanged: (Boolean) -> Unit,
    onKeepScreenAwakeChanged: (Boolean) -> Unit,
    onTheaterModeChanged: (Boolean) -> Unit,
    onHapticFeedbackChanged: (Boolean) -> Unit,
    onSmoothPressChanged: (Boolean) -> Unit,
    onAutoShowKeyboardChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.remote_preferences)) },
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
                .padding(top = Dimens.Space.xs, bottom = Dimens.Space.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.xl)
        ) {
            PreferenceSection(title = stringResource(R.string.section_connection)) {
                PreferenceRow(
                    icon = Icons.Outlined.Sync,
                    title = stringResource(R.string.auto_reconnect),
                    description = stringResource(R.string.auto_reconnect_description),
                    checked = preferencesUiState.isAutoReconnectEnabled,
                    onCheckedChange = onAutoReconnectChanged
                )
                PreferenceDivider()
                PreferenceRow(
                    icon = Icons.Outlined.LightMode,
                    title = stringResource(R.string.keep_screen_awake),
                    description = stringResource(R.string.keep_screen_awake_description),
                    checked = preferencesUiState.isKeepScreenAwakeEnabled,
                    onCheckedChange = onKeepScreenAwakeChanged
                )
                PreferenceDivider()
                PreferenceRow(
                    icon = Icons.Outlined.Theaters,
                    title = stringResource(R.string.theater_mode),
                    description = stringResource(R.string.theater_mode_description),
                    checked = preferencesUiState.isTheaterModeEnabled,
                    onCheckedChange = onTheaterModeChanged
                )
            }

            PreferenceSection(title = stringResource(R.string.section_remote_behavior)) {
                PreferenceRow(
                    icon = Icons.Outlined.Vibration,
                    title = stringResource(R.string.haptic_feedback),
                    description = stringResource(R.string.vibrate_when_buttons_are_pressed),
                    checked = preferencesUiState.isHapticFeedbackEnabled,
                    onCheckedChange = onHapticFeedbackChanged
                )
                PreferenceDivider()
                PreferenceRow(
                    icon = Icons.Outlined.TouchApp,
                    title = stringResource(R.string.smooth_press),
                    description = stringResource(R.string.repeat_commands_while_holding_buttons),
                    checked = preferencesUiState.isSmoothPressEnabled,
                    onCheckedChange = onSmoothPressChanged
                )
                PreferenceDivider()
                PreferenceRow(
                    icon = Icons.Outlined.Keyboard,
                    title = stringResource(R.string.auto_keyboard),
                    description = stringResource(R.string.automatically_show_the_keyboard_when_text_input_is_needed),
                    checked = preferencesUiState.isAutoShowKeyboardEnabled,
                    onCheckedChange = onAutoShowKeyboardChanged
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RemotePreferencesContentPreview() {
    RemoteLASTvTheme {
        RemotePreferencesContent(
            preferencesUiState = RemotePreferencesUiState(),
            onNavigateBack = {},
            onHapticFeedbackChanged = {},
            onSmoothPressChanged = {},
            onAutoShowKeyboardChanged = {},
            onAutoReconnectChanged = {},
            onKeepScreenAwakeChanged = {},
            onTheaterModeChanged = {}
        )
    }
}