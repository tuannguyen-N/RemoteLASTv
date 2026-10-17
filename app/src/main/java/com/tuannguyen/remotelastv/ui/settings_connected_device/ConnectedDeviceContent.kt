
package com.tuannguyen.remotelastv.ui.settings_connected_device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedDeviceContent(
    uiState: ConnectedDeviceUiState,
    onNavigateBack: () -> Unit,
    onEditClick: (Television) -> Unit,
    onEditedNameChange: (String) -> Unit,
    onConfirmRename: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onDeleteClick: (Television) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember {
        SnackbarHostState()
    },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.connected_device_title,
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.connected_device_back,
                            ),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding),
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = Dimens.Space.lg),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.connected_device_loading,
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = Dimens.Space.lg),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )

                        Spacer(
                            modifier = Modifier.size(Dimens.Space.md),
                        )

                        Button(onClick = onRetry) {
                            Text(
                                text = stringResource(
                                    R.string.connected_device_retry,
                                ),
                            )
                        }
                    }
                }

                uiState.devices.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = Dimens.Space.lg),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            modifier = Modifier.size(
                                Dimens.Size.iconLarge,
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(
                            modifier = Modifier.size(Dimens.Space.md),
                        )

                        Text(
                            text = stringResource(
                                R.string.connected_device_empty,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = Dimens.Space.lg),
                        verticalArrangement = Arrangement.spacedBy(
                            Dimens.Space.md,
                        ),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.connected_device_description,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(
                                Dimens.Space.md,
                            ),
                        ) {
                            items(
                                items = uiState.devices,
                                key = { television -> television.id },
                            ) { television ->
                                DeviceInformationCard(
                                    television = television,
                                    onEditClick = {
                                        onEditClick(television)
                                    },
                                    onDeleteClick = {
                                        onDeleteClick(television)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        uiState.editingDevice?.let { television ->
            AlertDialog(
                onDismissRequest = onDismissEditDialog,
                title = {
                    Text(
                        text = stringResource(
                            R.string.connected_device_edit_dialog_title,
                        ),
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            Dimens.Space.xs,
                        ),
                    ) {
                        OutlinedTextField(
                            value = uiState.editedName,
                            onValueChange = onEditedNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = {
                                Text(
                                    text = stringResource(
                                        R.string.connected_device_name_label,
                                    ),
                                )
                            },
                            isError = uiState.editedName.isBlank(),
                        )

                        if (uiState.editedName.isBlank()) {
                            Text(
                                text = stringResource(
                                    R.string.connected_device_name_error,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmRename,
                        enabled = uiState.editedName.isNotBlank() &&
                                uiState.editedName.trim() != television.name,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.connected_device_save,
                            ),
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissEditDialog) {
                        Text(
                            text = stringResource(
                                R.string.connected_device_cancel,
                            ),
                        )
                    }
                },
            )
        }

        uiState.deletingDevice?.let { television ->
            AlertDialog(
                onDismissRequest = onDismissDeleteDialog,
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                title = {
                    Text(
                        text = stringResource(
                            R.string.connected_device_delete_dialog_title,
                        ),
                    )
                },
                text = {
                    Text(
                        text = stringResource(
                            R.string.connected_device_delete_dialog_message,
                            television.name,
                        ),
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.connected_device_remove_action,
                            ),
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDeleteDialog) {
                        Text(
                            text = stringResource(
                                R.string.connected_device_cancel,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun DeviceInformationCard(
    television: Television,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.Space.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space.md),
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.Size.iconLarge),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space.xxs),
                ) {
                    Text(
                        text = television.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = television.brand.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(
                            R.string.connected_device_edit,
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(
                            R.string.connected_device_remove,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.Space.xxs),
            ) {
                Text(
                    text = stringResource(
                        R.string.connected_device_ip_address,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = television.ipAddress,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

private val previewTelevisions = listOf(
    Television(
        id = "tv-001",
        ipAddress = "192.168.1.100",
        name = "Living Room TV",
        brand = TelevisionBrand.UNKNOWN,
    ),
    Television(
        id = "tv-002",
        ipAddress = "192.168.1.101",
        name = "Bedroom TV",
        brand = TelevisionBrand.UNKNOWN,
    ),
)

@Preview(showBackground = true)
@Composable
private fun ConnectedDeviceListPreview() {
    RemoteLASTvTheme {
        ConnectedDeviceContent(
            uiState = ConnectedDeviceUiState(
                devices = previewTelevisions,
            ),
            onNavigateBack = {},
            onEditClick = {},
            onEditedNameChange = {},
            onConfirmRename = {},
            onDismissEditDialog = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onDismissDeleteDialog = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectedDeviceEditDialogPreview() {
    RemoteLASTvTheme {
        ConnectedDeviceContent(
            uiState = ConnectedDeviceUiState(
                devices = previewTelevisions,
                editingDevice = previewTelevisions.first(),
                editedName = "Living Room TV",
            ),
            onNavigateBack = {},
            onEditClick = {},
            onEditedNameChange = {},
            onConfirmRename = {},
            onDismissEditDialog = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onDismissDeleteDialog = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectedDeviceDeleteDialogPreview() {
    RemoteLASTvTheme {
        ConnectedDeviceContent(
            uiState = ConnectedDeviceUiState(
                devices = previewTelevisions,
                deletingDevice = previewTelevisions.first(),
            ),
            onNavigateBack = {},
            onEditClick = {},
            onEditedNameChange = {},
            onConfirmRename = {},
            onDismissEditDialog = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onDismissDeleteDialog = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectedDeviceLoadingPreview() {
    RemoteLASTvTheme {
        ConnectedDeviceContent(
            uiState = ConnectedDeviceUiState(isLoading = true),
            onNavigateBack = {},
            onEditClick = {},
            onEditedNameChange = {},
            onConfirmRename = {},
            onDismissEditDialog = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onDismissDeleteDialog = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectedDeviceEmptyPreview() {
    RemoteLASTvTheme {
        ConnectedDeviceContent(
            uiState = ConnectedDeviceUiState(),
            onNavigateBack = {},
            onEditClick = {},
            onEditedNameChange = {},
            onConfirmRename = {},
            onDismissEditDialog = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onDismissDeleteDialog = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectedDeviceErrorPreview() {
    RemoteLASTvTheme {
        ConnectedDeviceContent(
            uiState = ConnectedDeviceUiState(
                errorMessage = "Unable to load devices.",
            ),
            onNavigateBack = {},
            onEditClick = {},
            onEditedNameChange = {},
            onConfirmRename = {},
            onDismissEditDialog = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onDismissDeleteDialog = {},
            onRetry = {},
        )
    }
}
