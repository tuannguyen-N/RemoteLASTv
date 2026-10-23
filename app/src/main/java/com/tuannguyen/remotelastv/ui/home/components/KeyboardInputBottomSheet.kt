package com.tuannguyen.remotelastv.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardInputBottomSheet(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val textState = rememberTextFieldState(initialText = query)

    val singleCharDelete = InputTransformation {
        val removed = originalText.length - length
        if (removed > 1 && originalText.startsWith(asCharSequence())) {
            replace(0, length, originalText.dropLast(1))
        }
    }

    LaunchedEffect(textState) {
        snapshotFlow { textState.text.toString() }
            .collect(onQueryChange)
    }

    LaunchedEffect(query, textState) {
        if (textState.text.toString() != query) {
            textState.edit {
                replace(0, length, query)
            }
        }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }
            .first { it == SheetValue.Expanded }

        focusRequester.requestFocus()
        keyboardController?.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = MaterialTheme.shapes.extraLarge.topStart,
            topEnd = MaterialTheme.shapes.extraLarge.topEnd,
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        ),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = Dimens.Space.sm)
                    .size(
                        width = 40.dp,
                        height = Dimens.Space.xxs,
                    ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurfaceVariant
                    .copy(alpha = 0.35f),
            ) {}
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenPadding)
                .padding(top = Dimens.Space.xxl),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.xs),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.Size.minTouchTarget)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.large,
                    )
                    .padding(
                        horizontal = Dimens.Space.md,
                        vertical = Dimens.Space.xs,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.Size.iconMedium),
                )

                Spacer(Modifier.size(Dimens.Space.md))

                BasicTextField(
                    state = textState,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = Dimens.Size.minTouchTarget)
                        .focusRequester(focusRequester),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    inputTransformation = singleCharDelete,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                    ),
                    cursorBrush = SolidColor(
                        MaterialTheme.colorScheme.primary,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search,
                    ),
                    onKeyboardAction = { onSearch() },
                    decorator = { innerTextField ->
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            if (textState.text.isEmpty()) {
                                Text(
                                    text = stringResource(
                                        R.string.keyboard_input_hint,
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                if (textState.text.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                    ) {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space.xs),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.Size.iconSmall),
                )

                Text(
                    text = stringResource(
                        R.string.keyboard_input_tv_hint,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Dimens.Space.xxl))

            Spacer(Modifier.height(Dimens.Space.lg))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KeyboardInputBottomSheetPreview() {
    RemoteLASTvTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            KeyboardInputBottomSheet(
                query = "",
                onDismiss = {},
                onSearch = {},
                onQueryChange = {},
            )
        }
    }
}
