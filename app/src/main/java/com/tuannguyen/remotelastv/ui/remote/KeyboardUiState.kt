package com.tuannguyen.remotelastv.ui.remote

import com.tuannguyen.remotelastv.domain.model.TextInputState

data class KeyboardUiState(
    val query: String = "",
    val input: TextInputState = TextInputState.Idle,
    val inputHint: String? = null,
    val canObserveFocus: Boolean = false,
    val showKeyboardSheet: Boolean = false,
)