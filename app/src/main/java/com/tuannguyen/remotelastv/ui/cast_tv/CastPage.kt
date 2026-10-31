package com.tuannguyen.remotelastv.ui.cast_tv

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.domain.model.ConnectionState

@Composable
fun CastPage(
    modifier: Modifier = Modifier,
    connectionState: ConnectionState,
    openCast: (CastMediaType,Uri) -> Unit,
    viewModel: CastTvViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { openCast(CastMediaType.IMAGE, uri) }
    }

    val pickVideo = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {openCast(CastMediaType.VIDEO, uri) }
    }

    val pickAudio = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {openCast(CastMediaType.AUDIO, uri) }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CastUiEvent.PickMedia -> {
                    when (event.type) {
                        CastMediaType.IMAGE -> pickImage.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )

                        CastMediaType.VIDEO -> pickVideo.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.VideoOnly
                            )
                        )

                        CastMediaType.AUDIO -> pickAudio.launch("audio/*")
                    }
                }
            }
        }
    }

    val connectedTvName =
        (connectionState as? ConnectionState.Connected)?.tv?.name

    CastTvContent(
        uiState = uiState.copy(connectedTvName = connectedTvName),
        onPick = viewModel::onMediaTypeSelected,
        modifier = modifier,
    )
}