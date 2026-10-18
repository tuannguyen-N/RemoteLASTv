package com.tuannguyen.remotelastv.ui.home

import com.tuannguyen.remotelastv.domain.model.TextInputState

data class KeyboardUiState(
    val query: String = "",
    val input: TextInputState = TextInputState.Idle,
    val canObserveFocus: Boolean = false,
    val showKeyboardSheet: Boolean = false,
)