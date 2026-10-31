package com.tuannguyen.remotelastv.ui.cast_video

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.ui.common.messageResource
import com.tuannguyen.remotelastv.ui.device_discovery.components.DialogConnectionFailure

@Composable
fun CastVideoScreen(
    viewModel: CastVideoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val TAG = "CastVideoScreen"
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var failureReason by remember { mutableStateOf<CastFailReason?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CastVideoEvent.CastFailed -> {
                    Log.e(TAG, "Cast failed: ${event.reason}")
                    failureReason = event.reason
                }

                CastVideoEvent.NavigateBack -> {
                    Log.d(TAG, "Navigate back")
                    onNavigateBack()
                }

                is CastVideoEvent.ShowMessage -> {
                    Log.e(TAG, "Cast command failed: ${event.reason}")
                    failureReason = event.reason
                }
            }
        }
    }

    CastVideoContent(
        uiState = uiState,
        onPlayPauseClick = viewModel::onPlayPauseClick,
        onRewindClick = viewModel::onRewindClick,
        onForwardClick = viewModel::onForwardClick,
        onVolumeUpClick = viewModel::onVolumeUpClick,
        onVolumeDownClick = viewModel::onVolumeDownClick,
        onMuteClick = viewModel::onMuteClick,
        onStopCastingClick = viewModel::onStopCastingClick,
        onSeekTo = viewModel::onSeekTo,
    )

    failureReason?.let { reason ->
        DialogConnectionFailure(
            title = stringResource(R.string.cast_video_failure_title),
            errorMessage = stringResource(reason.messageResource()),
            retryLabel = stringResource(R.string.cast_video_try_again),
            onRetry = {
                failureReason = null
                viewModel.onRetryCasting()
            },
        )
    }
}