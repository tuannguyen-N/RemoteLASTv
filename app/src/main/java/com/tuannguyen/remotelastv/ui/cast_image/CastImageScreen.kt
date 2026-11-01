package com.tuannguyen.remotelastv.ui.cast_image

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.domain.model.CastFailReason

@Composable
fun CastImageScreen(
    viewModel: CastImageViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var failureReason by remember { mutableStateOf<CastFailReason?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(viewModel::onImageSelected) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CastImageEvent.CastFailed -> failureReason = event.reason
                CastImageEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    CastImageContent(
        uiState = uiState,
        onChangeImageClick = {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onStopCastingClick = viewModel::onStopCastingClick,
        failureReason = failureReason,
        onRetry = {
            failureReason = null
            viewModel.onRetryCasting()
        }
    )
}