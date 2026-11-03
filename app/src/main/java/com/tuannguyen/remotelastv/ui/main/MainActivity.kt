package com.tuannguyen.remotelastv.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.data.ads.AdsConsentManager
import com.tuannguyen.remotelastv.data.ads.AdsInitializer
import com.tuannguyen.remotelastv.ui.common.LocalHapticEnabled
import com.tuannguyen.remotelastv.ui.common.LocalHoldToRepeatEnabled
import com.tuannguyen.remotelastv.ui.navigation.AppNavHost
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var consentManager: AdsConsentManager
    @Inject
    lateinit var adsInitializer: AdsInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val isHapticEnabled by mainViewModel.isHapticEnabled.collectAsStateWithLifecycle()
            val isSmoothPressEnabled by mainViewModel.isSmoothPressEnabled.collectAsStateWithLifecycle()

            CompositionLocalProvider(
                LocalHapticEnabled provides isHapticEnabled,
                LocalHoldToRepeatEnabled provides isSmoothPressEnabled
            ) {
                RemoteLASTvTheme {
                    AppNavHost()
                }
            }
        }

        adsInitializer.initializeIfAllowed()

        if (savedInstanceState == null) {
            consentManager.gatherConsent(this) {
                adsInitializer.initializeIfAllowed()
            }
        }
    }
}