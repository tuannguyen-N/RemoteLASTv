package com.tuannguyen.remotelastv.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Preview
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onAction: (SettingsAction) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { action ->
            onAction(action)
        }
    }

    SettingsContent(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}