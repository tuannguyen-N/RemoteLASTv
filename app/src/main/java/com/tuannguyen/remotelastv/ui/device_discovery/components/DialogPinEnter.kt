package com.tuannguyen.remotelastv.ui.device_discovery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.common.clickableNoEffect
import com.tuannguyen.remotelastv.ui.theme.Dimens
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

private const val PIN_LENGTH = 6

@Composable
fun DialogPinEnter(
    modifier: Modifier = Modifier,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    val canSubmit = pin.length == PIN_LENGTH

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        shape = MaterialTheme.shapes.large,
        title = {
            Text(
                text = stringResource(R.string.pin_dialog_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space.md),
            ) {
                Text(
                    text = stringResource(R.string.pin_dialog_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                BasicTextField(
                    value = pin,
                    onValueChange = { value ->
                        pin = value
                            .uppercase()
                            .take(PIN_LENGTH)

                    },
                    enabled = true,
                    modifier = Modifier
                        .size(width = 1.dp, height = 1.dp)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canSubmit) {
                                keyboardController?.hide()
                                onSubmit(pin)
                            }
                        },
                    ),
                    decorationBox = { innerTextField ->
                        innerTextField()
                    },
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableNoEffect {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                    horizontalArrangement = Arrangement.spacedBy(
                        Dimens.Space.xs,
                        Alignment.CenterHorizontally,
                    ),
                ) {
                    repeat(PIN_LENGTH) { index ->
                        val isFilled = index < pin.length
                        val isActive = index == pin.length
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .size(Dimens.Size.minTouchTarget)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = MaterialTheme.shapes.small,
                                )
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        isActive -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline
                                    },
                                    shape = MaterialTheme.shapes.small,
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = pin.getOrNull(index)?.toString().orEmpty(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = {
                    if (canSubmit) {
                        keyboardController?.hide()
                        onSubmit(pin)
                    }
                },
            ) {
                Text(stringResource(R.string.pin_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.pin_cancel), color = MaterialTheme.colorScheme.error)
            }
        },
    )
}


@Preview(
    name = "PIN - Empty",
    showBackground = true,
)
@Composable
private fun PinEntryDialogEmptyPreview() {
    RemoteLASTvTheme {
        DialogPinEnter(
            onSubmit = {},
            onCancel = {},
        )
    }
}

@Preview(
    name = "PIN - Error",
    showBackground = true,
)
@Composable
private fun PinEntryDialogErrorPreview() {
    RemoteLASTvTheme {
        DialogPinEnter(
            onSubmit = {},
            onCancel = {},
        )
    }
}

@Preview(
    name = "PIN - Submitting",
    showBackground = true,
)
@Composable
private fun PinEntryDialogSubmittingPreview() {
    RemoteLASTvTheme {
        DialogPinEnter(
            onSubmit = {},
            onCancel = {},
        )
    }
}