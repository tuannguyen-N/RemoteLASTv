package com.tuannguyen.remotelastv.ui.cast_audio.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.AudioTrack
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import java.text.Normalizer
import java.util.Locale

@Composable
fun MusicPickerDialog(
    tracks: List<AudioTrack>,
    onTrackClick: (AudioTrack) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    selectedUris: Set<String> = emptySet(),
    isLoading: Boolean = false,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        MusicPickerContent(
            tracks = tracks,
            selectedUris = selectedUris,
            isLoading = isLoading,
            onTrackClick = onTrackClick,
            onDismiss = onDismiss,
            modifier = modifier
                .padding(horizontal = Dimens.Space.lg)
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
        )
    }
}

@Composable
fun MusicPickerContent(
    tracks: List<AudioTrack>,
    selectedUris: Set<String>,
    isLoading: Boolean,
    onTrackClick: (AudioTrack) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }

    val searchIndex = remember(tracks) { tracks.map { it to it.title.normalizedForSearch() } }
    val tokens = remember(query) {
        query.normalizedForSearch().split(' ').filter { it.isNotBlank() }
    }
    val results = remember(searchIndex, tokens) {
        if (tokens.isEmpty()) {
            tracks
        } else {
            searchIndex
                .filter { (_, title) -> tokens.all { title.contains(it) } }
                .map { it.first }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(tokens) { listState.scrollToItem(0) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.Radius.card),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PickerHeader(
                resultCount = if (isLoading || tracks.isEmpty()) null else results.size,
                onDismiss = onDismiss,
            )

            SearchField(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.padding(horizontal = Dimens.Space.lg),
            )

            Spacer(Modifier.height(Dimens.Space.sm))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                    tracks.isEmpty() -> PickerEmptyState(
                        icon = Icons.Rounded.MusicOff,
                        message = stringResource(R.string.music_picker_empty_library),
                    )

                    results.isEmpty() -> PickerEmptyState(
                        icon = Icons.Rounded.SearchOff,
                        message = stringResource(R.string.music_picker_no_results, query.trim()),
                    )

                    else -> LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = Dimens.Space.sm),
                    ) {
                        items(items = results, key = { it.uri }) { track ->
                            val selected = track.uri in selectedUris
                            AudioTrackRow(
                                track = track,
                                highlighted = selected,
                                onClick = { onTrackClick(track) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerHeader(
    resultCount: Int?,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.Space.lg,
                end = Dimens.Space.sm,
                top = Dimens.Space.sm,
                bottom = Dimens.Space.xs,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.music_picker_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (resultCount != null) {
                Text(
                    text = pluralStringResource(
                        R.plurals.music_picker_song_count,
                        resultCount,
                        resultCount,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.music_picker_close),
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val container = MaterialTheme.colorScheme.surfaceContainerHighest

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(Dimens.Radius.controlSurface),
        placeholder = { Text(stringResource(R.string.music_picker_search_hint)) },
        leadingIcon = {
            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.music_picker_clear),
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = container,
            unfocusedContainerColor = container,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun PickerEmptyState(
    icon: ImageVector,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.Size.castThumbnailPlaceholder),
        )
        Spacer(Modifier.height(Dimens.Space.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private val CombiningMarks = "\\p{Mn}+".toRegex()

private fun String.normalizedForSearch(): String =
    Normalizer.normalize(lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(CombiningMarks, "")
        .replace('đ', 'd')