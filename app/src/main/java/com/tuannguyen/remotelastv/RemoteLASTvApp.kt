package com.tuannguyen.remotelastv

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.connectsdk.discovery.DiscoveryManager
import com.tuannguyen.remotelastv.data.di.ApplicationScope
import com.tuannguyen.remotelastv.domain.repository.PremiumRepository
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class RemoteLASTvApp : Application() {
    @Inject
    lateinit var premiumRepository: PremiumRepository

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        DiscoveryManager.init(this)
        DiscoveryManager.getInstance().pairingLevel = DiscoveryManager.PairingLevel.ON
        DiscoveryManager.getInstance().registerDefaultDeviceTypes()

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                appScope.launch { premiumRepository.refresh() }
            }
        })
    }
}