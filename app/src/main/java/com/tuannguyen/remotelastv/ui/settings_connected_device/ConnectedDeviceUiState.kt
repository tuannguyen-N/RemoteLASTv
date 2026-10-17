package com.tuannguyen.remotelastv.ui.settings_connected_device

import com.tuannguyen.remotelastv.domain.model.Television

data class ConnectedDeviceUiState(
    val devices: List<Television> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val editingDevice: Television? = null,
    val deletingDevice: Television? = null,
    val editedName: String = "",
)