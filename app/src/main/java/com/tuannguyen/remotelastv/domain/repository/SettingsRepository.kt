package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.RemoteSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val remoteSettings: Flow<RemoteSettings>
    suspend fun setShowMediaButtons(show: Boolean)
    suspend fun setNavigationMode(mode: NavigationMode)
}