package com.tuannguyen.remotelastv.ui.cast_tv

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.domain.model.ConnectionState
import com.tuannguyen.remotelastv.ui.utils.findActivity
import kotlinx.coroutines.launch

@Composable
fun CastPage(
    modifier: Modifier = Modifier,
    connectionState: ConnectionState,
    openCast: (CastMediaType, Uri) -> Unit,
    openAudio: () -> Unit,
    viewModel: CastTvViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasRequestedAudioPermission by rememberUpdatedState(uiState.hasRequestedAudioPermission)
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val audioPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { openCast(CastMediaType.IMAGE, uri) }
    }

    val pickVideo = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { openCast(CastMediaType.VIDEO, uri) }
    }

    val requestAudioPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onAudioPermissionRequestCompleted()
        if (granted) {
            openAudio()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.cast_audio_permission_denied),
                )
            }
        }
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

                        CastMediaType.AUDIO -> {
                            when {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    audioPermission,
                                ) == PackageManager.PERMISSION_GRANTED -> openAudio()

                                hasRequestedAudioPermission &&
                                        activity != null &&
                                        !ActivityCompat.shouldShowRequestPermissionRationale(
                                            activity,
                                            audioPermission,
                                        ) -> {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data =
                                                Uri.fromParts("package", context.packageName, null)
                                        },
                                    )
                                }

                                else -> requestAudioPermission.launch(audioPermission)
                            }
                        }
                    }
                }
            }
        }
    }

    val connectedTvName =
        (connectionState as? ConnectionState.Connected)?.tv?.name

    Box(modifier = modifier.fillMaxSize()) {
        CastTvContent(
            uiState = uiState.copy(connectedTvName = connectedTvName),
            onPick = viewModel::onMediaTypeSelected,
            modifier = Modifier.fillMaxSize(),
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}