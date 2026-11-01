package com.tuannguyen.remotelastv.ui.cast_tv

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.domain.model.CastMediaType
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

private val VideoMotifColors = listOf(Color(0xFFF57E0B), Color(0xFFFF0640))
private val ImageMotifColors = listOf(Color(0xFF8BC34A), Color(0xFF26A66A))
private val AudioMotifColors = listOf(Color(0xFF29C5D8), Color(0xFF2878E5))

@Composable
fun CastTvContent(
    uiState: CastTvUiState,
    onPick: (CastMediaType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Text(
            text = "What do you want to cast?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        )

        MediaTile(
            type = CastMediaType.VIDEO,
            title = "Video",
            hint = "MP4, MKV, WebM",
            selected = uiState.selectedMediaType == CastMediaType.VIDEO,
            enabled = uiState.isConnected,
            onClick = { onPick(CastMediaType.VIDEO) },
            shape = RoundedCornerShape(28.dp),
            motifSize = 120.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MediaTile(
                type = CastMediaType.IMAGE,
                title = "Photo",
                hint = "JPG, PNG, WebP",
                selected = uiState.selectedMediaType == CastMediaType.IMAGE,
                enabled = uiState.isConnected,
                onClick = { onPick(CastMediaType.IMAGE) },
                shape = RoundedCornerShape(20.dp),
                motifSize = 64.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(168.dp),
            )
            MediaTile(
                type = CastMediaType.AUDIO,
                title = "Audio",
                hint = "MP3, AAC, FLAC",
                selected = uiState.selectedMediaType == CastMediaType.AUDIO,
                enabled = uiState.isConnected,
                onClick = { onPick(CastMediaType.AUDIO) },
                shape = RoundedCornerShape(20.dp),
                motifSize = 64.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(168.dp),
            )
        }
    }
}

@Composable
private fun MediaTile(
    type: CastMediaType,
    title: String,
    hint: String,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    motifSize: Dp,
    modifier: Modifier = Modifier,
) {
    val titleColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.alpha(if (enabled) 1f else 0.45f),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Motif(
                type = type,
                colors = when (type) {
                    CastMediaType.VIDEO -> VideoMotifColors
                    CastMediaType.IMAGE -> ImageMotifColors
                    CastMediaType.AUDIO -> AudioMotifColors
                },
                modifier = Modifier
                    .size(motifSize)
                    .align(Alignment.TopEnd),
            )
            Column(Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = titleColor,
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = titleColor.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
private fun Motif(type: CastMediaType, colors: List<Color>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            brush = Brush.linearGradient(
                colors.map { it.copy(alpha = 0.16f) },
            ),
            cornerRadius = CornerRadius(w * 0.18f),
        )
        val brush = Brush.linearGradient(colors)
        when (type) {
            CastMediaType.VIDEO -> {
                val play = Path().apply {
                    moveTo(w * 0.40f, h * 0.28f)
                    lineTo(w * 0.40f, h * 0.72f)
                    lineTo(w * 0.74f, h * 0.50f)
                    close()
                }
                drawPath(play, brush)
            }

            CastMediaType.IMAGE -> {
                drawCircle(brush, radius = w * 0.09f, center = Offset(w * 0.68f, h * 0.30f))
                val hills = Path().apply {
                    moveTo(w * 0.14f, h * 0.80f)
                    lineTo(w * 0.42f, h * 0.44f)
                    lineTo(w * 0.58f, h * 0.64f)
                    lineTo(w * 0.68f, h * 0.54f)
                    lineTo(w * 0.86f, h * 0.80f)
                    close()
                }
                drawPath(hills, brush)
            }

            CastMediaType.AUDIO -> {
                val heights = listOf(0.30f, 0.60f, 0.42f, 0.72f, 0.36f)
                val barW = w * 0.10f
                val gap = w * 0.055f
                val total = heights.size * barW + (heights.size - 1) * gap
                val startX = (w - total) / 2f
                heights.forEachIndexed { i, fraction ->
                    val barH = h * fraction
                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(startX + i * (barW + gap), (h - barH) / 2f),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f),
                    )
                }
            }
        }
    }
}

@Preview(name = "Connected · dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CastTvContentConnectedPreview() {
    RemoteLASTvTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CastTvContent(
                uiState = CastTvUiState(connectedTvName = "Living room TV"),
                onPick = {},
            )
        }
    }
}

@Preview(name = "No TV · dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CastTvContentDisconnectedPreview() {
    RemoteLASTvTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CastTvContent(
                uiState = CastTvUiState(),
                onPick = {},
            )
        }
    }
}

@Preview(name = "Connected · light", showBackground = true)
@Composable
private fun CastTvContentLightPreview() {
    RemoteLASTvTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CastTvContent(
                uiState = CastTvUiState(connectedTvName = "Living room TV"),
                onPick = {},
            )
        }
    }
}