package com.tuannguyen.remotelastv.domain.usecase

import com.tuannguyen.remotelastv.domain.connection.TvConnectionManager
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.domain.repository.SavedTvRepository
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ReconnectLastTvUseCase @Inject constructor(
    private val savedTvRepository: SavedTvRepository,
    private val connectionManager: TvConnectionManager,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): ConnectionState {
        val current = connectionManager.state.value
        if (current is ConnectionState.Connected ||
            current is ConnectionState.Connecting ||
            current is ConnectionState.Reconnecting
        ) return current

        if (!settingsRepository.remoteSettings.first().isAutoReconnectEnabled) return ConnectionState.Idle
        val last = savedTvRepository.getLastConnected() ?: return ConnectionState.Idle

        return connectionManager.connect(last, showConnectionState = false)
            .getOrNull()
            ?.let { ConnectionState.Connected(it) }
            ?: ConnectionState.Idle
    }
}