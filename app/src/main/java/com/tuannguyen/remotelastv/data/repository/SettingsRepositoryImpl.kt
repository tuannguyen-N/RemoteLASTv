package com.tuannguyen.remotelastv.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tuannguyen.remotelastv.domain.model.NavigationMode
import com.tuannguyen.remotelastv.domain.model.RemoteSettings
import com.tuannguyen.remotelastv.domain.repository.CastAdCounterRepository
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository, CastAdCounterRepository {

    private object Keys {
        val SHOW_MEDIA_BUTTONS = booleanPreferencesKey("show_media_buttons")
        val NAVIGATION_MODE = stringPreferencesKey("navigation_mode")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val SMOOTH_PRESS_ENABLED = booleanPreferencesKey("smooth_press_enabled")
        val AUTO_RECONNECT_ENABLED = booleanPreferencesKey("auto_reconnect_enabled")
        val KEEP_SCREEN_AWAKE_ENABLED = booleanPreferencesKey("keep_screen_awake_enabled")
        val THEATER_MODE_ENABLED = booleanPreferencesKey("theater_mode_enabled")
        val AUTO_SHOW_KEYBOARD_ENABLED = booleanPreferencesKey("auto_show_keyboard_enabled")
        val HAS_CONNECTED_KEY = booleanPreferencesKey("has_seen_remote_tutorial")
        val HAS_REQUESTED_MICROPHONE_PERMISSION =
            booleanPreferencesKey("has_requested_microphone_permission")
        val HAS_REQUESTED_AUDIO_PERMISSION =
            booleanPreferencesKey("has_requested_audio_permission")
        val SUCCESSFUL_CAST_COUNT = intPreferencesKey("successful_cast_count")
        val LAST_INTERSTITIAL_SHOWN_AT = longPreferencesKey("last_interstitial_shown_at")
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
                isHapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK_ENABLED]
                    ?: default.isHapticFeedbackEnabled,
                isSmoothPressEnabled = prefs[Keys.SMOOTH_PRESS_ENABLED]
                    ?: default.isSmoothPressEnabled,
                isAutoReconnectEnabled = prefs[Keys.AUTO_RECONNECT_ENABLED]
                    ?: default.isAutoReconnectEnabled,
                isKeepScreenAwakeEnabled = prefs[Keys.KEEP_SCREEN_AWAKE_ENABLED]
                    ?: default.isKeepScreenAwakeEnabled,
                isTheaterModeEnabled = prefs[Keys.THEATER_MODE_ENABLED]
                    ?: default.isTheaterModeEnabled,
                hasSeenRemoteTutorial = prefs[Keys.HAS_CONNECTED_KEY]
                    ?: default.hasSeenRemoteTutorial,
                hasRequestedMicrophonePermission =
                    prefs[Keys.HAS_REQUESTED_MICROPHONE_PERMISSION]
                        ?: default.hasRequestedMicrophonePermission,
                hasRequestedAudioPermission =
                    prefs[Keys.HAS_REQUESTED_AUDIO_PERMISSION]
                        ?: default.hasRequestedAudioPermission,
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

    override suspend fun setAutoShowKeyboardEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.AUTO_SHOW_KEYBOARD_ENABLED] = enabled }
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

    override suspend fun setHasSeenRemoteTutorial(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.HAS_CONNECTED_KEY] = enabled }
    }

    override suspend fun setHasRequestedMicrophonePermission(requested: Boolean) {
        context.settingsDataStore.edit {
            it[Keys.HAS_REQUESTED_MICROPHONE_PERMISSION] = requested
        }
    }

    override suspend fun setHasRequestedAudioPermission(requested: Boolean) {
        context.settingsDataStore.edit {
            it[Keys.HAS_REQUESTED_AUDIO_PERMISSION] = requested
        }
    }

    override suspend fun recordSuccessfulCast() {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.SUCCESSFUL_CAST_COUNT] =
                (preferences[Keys.SUCCESSFUL_CAST_COUNT] ?: 0) + 1
        }
    }

    override suspend fun shouldShowInterstitial(
        threshold: Int,
        cooldownMillis: Long,
    ): Boolean =
        context.settingsDataStore.data
            .map { preferences ->
                val successfulCastCount = preferences[Keys.SUCCESSFUL_CAST_COUNT] ?: 0
                val lastShownAt = preferences[Keys.LAST_INTERSTITIAL_SHOWN_AT]
                val isOutsideCooldown = lastShownAt == null ||
                        System.currentTimeMillis() - lastShownAt >= cooldownMillis
                successfulCastCount >= threshold && isOutsideCooldown
            }
            .first()

    override suspend fun recordInterstitialShown() {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.SUCCESSFUL_CAST_COUNT] = 0
            preferences[Keys.LAST_INTERSTITIAL_SHOWN_AT] = System.currentTimeMillis()
        }
    }
}