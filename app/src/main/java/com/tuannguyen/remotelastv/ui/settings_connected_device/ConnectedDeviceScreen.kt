package com.tuannguyen.remotelastv.ui.settings_connected_device

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConnectedDeviceScreen(
    viewModel: ConnectedDeviceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ConnectedDeviceContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onEditClick = viewModel::onEditClick,
        onEditedNameChange = viewModel::onEditNameChange,
        onConfirmRename = viewModel::onConfirmRename,
        onDismissEditDialog = viewModel::onDismissEditDialog,
        onDeleteClick = viewModel::onDeleteClick,
        onConfirmDelete = viewModel::onConfirmDelete,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onRetry = viewModel::onRetry,
        snackbarHostState = snackbarHostState,
    )
}