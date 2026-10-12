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
            )
        }

    override suspend fun setShowMediaButtons(show: Boolean) {
        context.settingsDataStore.edit { it[Keys.SHOW_MEDIA_BUTTONS] = show }
    }

    override suspend fun setNavigationMode(mode: NavigationMode) {
        context.settingsDataStore.edit { it[Keys.NAVIGATION_MODE] = mode.name }
    }
}