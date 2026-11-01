package com.tuannguyen.remotelastv.domain.model

sealed interface TextInputState {
    data object Unknown : TextInputState
    data object Idle : TextInputState
    data class Focused(
        val text: String?,
        val keyboardType: TextKeyboardType?,
        val label: String? = null,
        val shouldAutoShowKeyboard: Boolean = true,
    ) : TextInputState
}

enum class TextKeyboardType {
    TEXT,
    NUMBER,
    DECIMAL,
    PHONE,
    EMAIL,
    PASSWORD,
    URI,
}