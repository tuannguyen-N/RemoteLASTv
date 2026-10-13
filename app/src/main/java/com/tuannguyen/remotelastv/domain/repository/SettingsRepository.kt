package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.RemoteSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val remoteSettings: Flow<RemoteSettings>
    suspend fun setShowMediaButtons(show: Boolean)
    suspend fun setNavigationMode(mode: NavigationMode)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setSmoothPressEnabled(enabled: Boolean)
    suspend fun setAutoReconnectEnabled(enabled: Boolean)
    suspend fun setKeepScreenAwakeEnabled(enabled: Boolean)
    suspend fun setTheaterModeEnabled(enabled: Boolean)
}