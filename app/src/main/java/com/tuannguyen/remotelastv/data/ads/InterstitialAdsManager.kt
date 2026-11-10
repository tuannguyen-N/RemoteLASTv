package com.tuannguyen.remotelastv.data.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.tuannguyen.remotelastv.BuildConfig
import com.tuannguyen.remotelastv.domain.model.InterstitialAdPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class InterstitialAdsManager @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
) {
    @Volatile
    private var interstitialAd: InterstitialAd? = null

    @Volatile
    private var isLoading = false

    @Volatile
    private var lastInterstitialShownElapsedRealtime: Long? = null

    fun load() {
        if (isLoading || interstitialAd != null) return
        isLoading = true
        InterstitialAd.load(
            appContext,
            BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                }
            },
        )
    }

    fun showIfReady(
        context: Context,
        onAdShown: () -> Unit,
        onFinished: () -> Unit,
    ): Boolean {
        val activity = context.findActivity() ?: return false
        val lastShownAt = lastInterstitialShownElapsedRealtime
        if (
            lastShownAt != null &&
            SystemClock.elapsedRealtime() - lastShownAt < InterstitialAdPolicy.COOLDOWN_MILLIS
        ) {
            return false
        }
        val ad = interstitialAd ?: run {
            load()
            return false
        }
        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastInterstitialShownElapsedRealtime = SystemClock.elapsedRealtime()
                onAdShown()
            }

            override fun onAdDismissedFullScreenContent() {
                load()
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                load()
                onFinished()
            }
        }
        return try {
            ad.show(activity)
            true
        } catch (error: RuntimeException) {
            Log.e(TAG, "Could not show interstitial ad", error)
            load()
            false
        }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    private companion object {
        const val TAG = "InterstitialAdsManager"
    }
}
