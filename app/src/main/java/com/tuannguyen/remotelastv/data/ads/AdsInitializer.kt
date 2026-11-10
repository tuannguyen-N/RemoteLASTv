package com.tuannguyen.remotelastv.data.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.tuannguyen.remotelastv.data.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
class AdsInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val consent: AdsConsentManager,
    @ApplicationScope private val appScope: CoroutineScope,
) {
    private val started = AtomicBoolean(false)
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    fun initializeIfAllowed() {
        if (!consent.canRequestAds || !started.compareAndSet(false, true)) return
        appScope.launch(Dispatchers.IO) {
            MobileAds.initialize(context) { _ready.value = true }
        }
    }
}