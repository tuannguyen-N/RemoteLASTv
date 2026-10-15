package com.tuannguyen.remotelastv.ui.device_discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.model.DiscoveryStatus
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.usecase.TvDiscoveryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceDiscoveryViewModel @Inject constructor(
    private val tvDiscoveryUseCase: TvDiscoveryUseCase,
    private val connectionManager: TvConnectionManager,
) : ViewModel() {

    private val _localState = MutableStateFlow(DeviceDiscoveryUiState())
    val uiState: StateFlow<DeviceDiscoveryUiState> = combine(
        _localState,
        connectionManager.state
    ) { localState, connectionState ->
        localState.copy(
            connectionState = connectionState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DeviceDiscoveryUiState()
    )

    private val _events = Channel<DiscoveryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var scanJob: Job? = null

    fun startScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _localState.update {
                it.copy(
                    discoveryStatus = DiscoveryStatus.Scanning,
                    televisions = emptyList()
                )
            }
            tvDiscoveryUseCase().collect { tv ->
                _localState.update { it.copy(televisions = it.televisions + tv) }
            }
            _localState.update { it.copy(discoveryStatus = DiscoveryStatus.Completed) }
        }
    }

    fun onRetry() {
        startScan()
    }

    fun connect(tv: Television) {
        viewModelScope.launch {
            connectionManager.connect(tv)
                .onSuccess { sendEvent(DiscoveryEvent.ConnectionSuccess(tv.name)) }
                .onFailure { sendEvent(DiscoveryEvent.ConnectionFailure("Reason: ${it.message}")) }
        }
    }

    fun submitPin(pin: String) = viewModelScope.launch { connectionManager.submitPin(pin) }
    fun disconnect(tv: Television) = connectionManager.disconnect()

    private fun sendEvent(event: DiscoveryEvent) = viewModelScope.launch { _events.send(event) }
}