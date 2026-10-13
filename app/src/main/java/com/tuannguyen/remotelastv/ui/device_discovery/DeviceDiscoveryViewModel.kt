package com.tuannguyen.remotelastv.ui.device_discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.model.DiscoveryStatus
import com.tuannguyen.remotelastv.domain.usecase.TvDiscoveryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceDiscoveryViewModel @Inject constructor(
    private val tvDiscoveryUseCase: TvDiscoveryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceDiscoveryUiState())
    val uiState = _uiState.asStateFlow()

    private var scanJob: Job? = null

    fun startScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = DiscoveryStatus.Scanning,
                    televisions = emptyList()
                )
            }
            tvDiscoveryUseCase().collect { tv ->
                _uiState.update { it.copy(televisions = it.televisions + tv) }
            }
            _uiState.update { it.copy(status = DiscoveryStatus.Completed) }
        }
    }

    fun onRetry() {
        startScan()
    }
}