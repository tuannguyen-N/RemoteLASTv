package com.tuannguyen.remotelastv.ui.cast_audio

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.ui.ads.interstitial.CastInterstitialViewModel

@Composable
fun CastAudioScreen(
    viewModel: CastAudioViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interstitialViewModel: CastInterstitialViewModel = hiltViewModel()
    val context = LocalContext.current
    var failureReason by remember { mutableStateOf<CastFailReason?>(null) }
    BackHandler(onBack = viewModel::onStopCastingClick)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CastAudioEvent.CastFailed -> {
                    Log.e(TAG, "Audio cast failed: ${event.reason}")
                    failureReason = event.reason
                }

                is CastAudioEvent.ShowMessage -> {
                    Log.e(TAG, "Audio cast command failed: ${event.reason}")
                    failureReason = event.reason
                }

                is CastAudioEvent.StopCompleted -> {
                    if (
                        !event.showInterstitial ||
                        !interstitialViewModel.showIfReady(context, onNavigateBack)
                    ) {
                        onNavigateBack()
                    }
                }
            }
        }
    }

    CastAudioContent(
        uiState = uiState,
        onPlayPauseClick = viewModel::onPlayPauseClick,
        onPreviousClick = viewModel::onPreviousClick,
        onNextClick = viewModel::onNextClick,
        onRepeatClick = viewModel::onRepeatClick,
        onShuffleClick = viewModel::onShuffleClick,
        onVolumeUpClick = viewModel::onVolumeUpClick,
        onVolumeDownClick = viewModel::onVolumeDownClick,
        onMuteClick = viewModel::onMuteClick,
        onStopCastingClick = viewModel::onStopCastingClick,
        onSeekTo = viewModel::onSeekTo,
        onTrackClick = viewModel::onTrackClick,
        onMusicPickerClick = viewModel::onMusicPickerRequested,
        onMusicPickerDismiss = viewModel::onMusicPickerDismissed,
        failureReason = failureReason,
        onRetry = {
            failureReason = null
            viewModel.onRetryCasting()
        },
    )
}

private const val TAG = "CastAudioScreen"
