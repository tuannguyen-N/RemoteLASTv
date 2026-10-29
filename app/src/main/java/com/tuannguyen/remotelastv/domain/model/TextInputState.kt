package com.tuannguyen.remotelastv.domain.model

import androidx.compose.ui.text.input.KeyboardType

sealed interface TextInputState {
    data object Unknown : TextInputState
    data object Idle : TextInputState
    data class Focused(
        val text: String?,
        val keyboardType: KeyboardType?,
        val label: String? = null,
        val shouldAutoShowKeyboard: Boolean = true,
    ) : TextInputState
}