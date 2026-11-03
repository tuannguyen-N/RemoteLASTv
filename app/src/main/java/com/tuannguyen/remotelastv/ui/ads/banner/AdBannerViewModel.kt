package com.tuannguyen.remotelastv.ui.ads.banner

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.tuannguyen.remotelastv.BuildConfig
import com.tuannguyen.remotelastv.data.ads.AdsInitializer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AdBannerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    adsInitializer: AdsInitializer,
) : ViewModel() {

    val adsReady: StateFlow<Boolean> = adsInitializer.ready

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private var adView: AdView? = null
    private var widthDp: Int = -1

    fun getOrCreate(screenWidthDp: Int): AdView {
        val existing = adView
        if (existing != null && widthDp == screenWidthDp) return existing

        existing?.destroy()
        _loaded.value = false
        widthDp = screenWidthDp

        return AdView(appContext).apply {
            setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(appContext, screenWidthDp)
            )
            adUnitId = BuildConfig.ADMOB_HOME_BANNER_AD_UNIT_ID
            adListener = object : AdListener() {
                override fun onAdLoaded() { _loaded.value = true }
                override fun onAdFailedToLoad(p0: LoadAdError) { _loaded.value = false }
            }
            loadAd(AdRequest.Builder().build())
        }.also { adView = it }
    }

    override fun onCleared() {
        adView?.destroy()
        adView = null
    }
}