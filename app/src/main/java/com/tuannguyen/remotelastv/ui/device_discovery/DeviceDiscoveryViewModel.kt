package com.tuannguyen.remotelastv.ui.device_discovery

import androidx.lifecycle.ViewModel
import com.tuannguyen.remotelastv.domain.model.Television
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceDiscoveryViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow<DeviceDiscoveryUiState>(
            DeviceDiscoveryUiState.Scanning()
        )

    val uiState = _uiState.asStateFlow()

    fun onTelevisionFound(televisions: List<Television>) {
        _uiState.value = DeviceDiscoveryUiState.Scanning(
            televisions = televisions,
        )
    }

    fun onDiscoveryCompleted(televisions: List<Television>) {
        _uiState.value = if (televisions.isEmpty()) {
            DeviceDiscoveryUiState.Empty
        } else {
            DeviceDiscoveryUiState.Success(
                televisions = televisions,
            )
        }
    }

    fun onRetry() {
        _uiState.value = DeviceDiscoveryUiState.Scanning()
    }
}