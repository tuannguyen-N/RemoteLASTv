package com.tuannguyen.remotelastv.ui.ads.banner

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.tuannguyen.remotelastv.BuildConfig
import com.tuannguyen.remotelastv.data.ads.AdsInitializer
import com.tuannguyen.remotelastv.domain.repository.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdBannerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    adsInitializer: AdsInitializer,
    premiumRepository: PremiumRepository,
) : ViewModel() {

    val adsReady: StateFlow<Boolean> = adsInitializer.ready

    private val _canShowAds = MutableStateFlow(false)
    val canShowAds: StateFlow<Boolean> = _canShowAds.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private var adView: AdView? = null
    private var widthDp: Int = -1

    init {
        viewModelScope.launch {
            combine(
                premiumRepository.hasSyncedPremiumStatus,
                premiumRepository.premiumStatus,
            ) { hasSyncedStatus, status ->
                hasSyncedStatus && !status.isPremium
            }.collect { canShowAds ->
                _canShowAds.value = canShowAds
                if (!canShowAds) releaseAdView()
            }
        }
    }

    fun getOrCreate(screenWidthDp: Int): AdView? {
        if (!_canShowAds.value) return null

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
                override fun onAdLoaded() {
                    _loaded.value = true
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    _loaded.value = false
                }
            }
            loadAd(AdRequest.Builder().build())
        }.also { adView = it }
    }

    private fun releaseAdView() {
        adView?.destroy()
        adView = null
        widthDp = -1
        _loaded.value = false
    }

    override fun onCleared() {
        releaseAdView()
    }
}