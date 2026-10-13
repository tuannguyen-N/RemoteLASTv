package com.tuannguyen.remotelastv.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.RemoteSettings
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private object Keys {
        val SHOW_MEDIA_BUTTONS = booleanPreferencesKey("show_media_buttons")
        val NAVIGATION_MODE = stringPreferencesKey("navigation_mode")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val SMOOTH_PRESS_ENABLED = booleanPreferencesKey("smooth_press_enabled")
        val AUTO_RECONNECT_ENABLED = booleanPreferencesKey("auto_reconnect_enabled")
        val KEEP_SCREEN_AWAKE_ENABLED = booleanPreferencesKey("keep_screen_awake_enabled")
        val THEATER_MODE_ENABLED = booleanPreferencesKey("theater_mode_enabled")
    }

    override val remoteSettings: Flow<RemoteSettings> = context.settingsDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val default = RemoteSettings()
            RemoteSettings(
                showMediaButtons = prefs[Keys.SHOW_MEDIA_BUTTONS] ?: default.showMediaButtons,
                navigationMode = NavigationMode.entries
                    .firstOrNull { it.name == prefs[Keys.NAVIGATION_MODE] }
                    ?: default.navigationMode,
                isHapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK_ENABLED] ?: default.isHapticFeedbackEnabled,
                isSmoothPressEnabled = prefs[Keys.SMOOTH_PRESS_ENABLED] ?: default.isSmoothPressEnabled,
                isAutoReconnectEnabled = prefs[Keys.AUTO_RECONNECT_ENABLED] ?: default.isAutoReconnectEnabled,
                isKeepScreenAwakeEnabled = prefs[Keys.KEEP_SCREEN_AWAKE_ENABLED] ?: default.isKeepScreenAwakeEnabled,
                isTheaterModeEnabled = prefs[Keys.THEATER_MODE_ENABLED] ?: default.isTheaterModeEnabled,
            )
        }

    override suspend fun setShowMediaButtons(show: Boolean) {
        context.settingsDataStore.edit { it[Keys.SHOW_MEDIA_BUTTONS] = show }
    }

    override suspend fun setNavigationMode(mode: NavigationMode) {
        context.settingsDataStore.edit { it[Keys.NAVIGATION_MODE] = mode.name }
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.HAPTIC_FEEDBACK_ENABLED] = enabled }
    }

    override suspend fun setSmoothPressEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.SMOOTH_PRESS_ENABLED] = enabled }
    }

    override suspend fun setAutoReconnectEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.AUTO_RECONNECT_ENABLED] = enabled }
    }

    override suspend fun setKeepScreenAwakeEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.KEEP_SCREEN_AWAKE_ENABLED] = enabled }
    }

    override suspend fun setTheaterModeEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.THEATER_MODE_ENABLED] = enabled }
    }
}