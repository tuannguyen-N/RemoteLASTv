package com.tuannguyen.remotelastv.ui.ads.banner

import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    viewModel: AdBannerViewModel = hiltViewModel(),
) {
    val ready by viewModel.adsReady.collectAsStateWithLifecycle()
    val loaded by viewModel.loaded.collectAsStateWithLifecycle()
    if (LocalInspectionMode.current || !ready) return

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val adView = remember(screenWidthDp) { viewModel.getOrCreate(screenWidthDp) }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { adView.pause() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { adView.resume() }

    Column(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (loaded) (adView.adSize?.height ?: 0).dp else 0.dp),
            factory = { adView },
            onRelease = { (it.parent as? ViewGroup)?.removeView(it) },
        )
    }
}