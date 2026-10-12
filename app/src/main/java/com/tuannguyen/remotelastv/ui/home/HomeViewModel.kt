package com.tuannguyen.remotelastv.ui.home

import androidx.lifecycle.ViewModel
import com.tuannguyen.remotelastv.domain.model.RemoteKey
import com.tuannguyen.remotelastv.domain.model.TvApp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onPowerClick() {
        // TODO: Send power command to the connected TV.
        _effects.trySend(HomeEffect.ShowMessage("Power control is not connected yet"))
    }

    fun onSettingsClick() {
        _effects.trySend(HomeEffect.ShowMessage("Settings screen is not implemented yet"))
    }

    fun onDeviceClick() {
        _effects.trySend(HomeEffect.ShowMessage("Device selector is not implemented yet"))
    }

    fun onAppClick(appName: TvApp) {
        // TODO: Implement launching the selected TV app.
        _effects.trySend(HomeEffect.ShowMessage("$appName control is not connected yet"))
    }

    fun onRemoteButtonClick(button: RemoteKey) {
        // TODO: Send remote-control command.
        _effects.trySend(HomeEffect.ShowMessage("$button command is not connected yet"))
    }

    fun onPageSelected(page: Int) {
        _uiState.update { it.copy(selectedPage = page.coerceIn(0, 1)) }
    }

    fun onRetry() {
        _uiState.update { it.copy(isLoading = false, errorMessage = null, isEmpty = false) }
    }
}
