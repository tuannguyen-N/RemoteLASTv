package com.tuannguyen.remotelastv.ui.ads.interstitial

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.tuannguyen.remotelastv.di.ApplicationScope
import com.tuannguyen.remotelastv.data.ads.InterstitialAdsManager
import com.tuannguyen.remotelastv.domain.usecase.StopCastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltViewModel
class CastInterstitialViewModel @Inject constructor(
    private val interstitialAdsManager: InterstitialAdsManager,
    private val stopCast: StopCastUseCase,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {
    fun showIfReady(context: Context, onFinished: () -> Unit): Boolean =
        interstitialAdsManager.showIfReady(
            context = context,
            onAdShown = {
                appScope.launch {
                    try {
                        stopCast.onInterstitialShown()
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        Log.e(TAG, "Failed to persist cast count after interstitial", error)
                    }
                }
            },
            onFinished = onFinished,
        )

    private companion object {
        const val TAG = "CastInterstitialVM"
    }
}
