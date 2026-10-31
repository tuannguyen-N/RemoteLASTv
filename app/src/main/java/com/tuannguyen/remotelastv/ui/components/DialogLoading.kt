package com.tuannguyen.remotelastv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme


@Composable
fun DialogLoading(
    message: String? = null,
    modal: Boolean = true,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading)
    )

    val content: @Composable () -> Unit = {
        LoadingContent(
            composition = composition,
            message = message
        )
    }

    if (modal) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            Box(
                modifier = Modifier.size(Dimens.Size.loadingDialog),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
                )
                .padding(horizontal = Dimens.Space.loadingOverlayHorizontal),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun LoadingContent(
    composition: LottieComposition?,
    message: String?,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.Space.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(Dimens.Size.loadingAnimation),
            )

            if (!message.isNullOrBlank()) {
                Spacer(Modifier.height(Dimens.Space.sm))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Loading Dialog")
@Composable
private fun DialogLoadingModalPreview() {
    RemoteLASTvTheme {
        DialogLoading(
            message = "Connecting to TV...",
            modal = true
        )
    }
}

@Preview(showBackground = true, name = "Loading Overlay")
@Composable
private fun DialogLoadingOverlayPreview() {
    RemoteLASTvTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "Cast TV",
                modifier = Modifier.align(Alignment.TopCenter)
            )

            DialogLoading(
                message = "Some videos may take longer than usual to start. Please wait.",
                modal = false
            )
        }
    }
}
