package com.tuannguyen.remotelastv.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tuannguyen.remotelastv.domain.model.PremiumPlanId
import com.tuannguyen.remotelastv.domain.model.PremiumStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.premiumDataStore by preferencesDataStore(name = "premium")

@Singleton
class PremiumLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val ACTIVE_PLAN_ID = stringPreferencesKey("active_plan_id")
        val INTERSTITIAL_AD_COUNT = intPreferencesKey("interstitial_ad_count")
    }

    val interstitialAdCount: Flow<Int> =
        context.premiumDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs -> prefs[Keys.INTERSTITIAL_AD_COUNT] ?: 0 }
            .distinctUntilChanged()

    val premiumStatus: Flow<PremiumStatus> =
        context.premiumDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs ->
                PremiumStatus(
                    isPremium = prefs[Keys.IS_PREMIUM] ?: false,
                    activePlanId = prefs[Keys.ACTIVE_PLAN_ID]
                        ?.let { value ->
                            runCatching {
                                PremiumPlanId.valueOf(value)
                            }.getOrNull()
                        },
                )
            }
            .distinctUntilChanged()

    suspend fun incrementInterstitialAdCount(): Int {
        var count = 0
        context.premiumDataStore.edit { prefs ->
            count = (prefs[Keys.INTERSTITIAL_AD_COUNT] ?: 0) + 1
            prefs[Keys.INTERSTITIAL_AD_COUNT] = count
        }
        return count
    }

    suspend fun resetInterstitialAdCount() {
        context.premiumDataStore.edit { prefs ->
            prefs[Keys.INTERSTITIAL_AD_COUNT] = 0
        }
    }

    suspend fun setPremiumStatus(status: PremiumStatus) {
        context.premiumDataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = status.isPremium

            if (status.activePlanId != null) {
                prefs[Keys.ACTIVE_PLAN_ID] = status.activePlanId.name
            } else {
                prefs.remove(Keys.ACTIVE_PLAN_ID)
            }
        }
    }
}