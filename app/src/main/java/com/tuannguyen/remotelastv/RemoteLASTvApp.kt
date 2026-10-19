package com.tuannguyen.remotelastv

import android.app.Application
import com.connectsdk.discovery.DiscoveryManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RemoteLASTvApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DiscoveryManager.init(this)
        DiscoveryManager.getInstance().pairingLevel = DiscoveryManager.PairingLevel.ON
        DiscoveryManager.getInstance().registerDefaultDeviceTypes()
    }
}