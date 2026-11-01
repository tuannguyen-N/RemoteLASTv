package com.tuannguyen.remotelastv.ui.cast_image

import android.net.Uri

enum class CastImagePhase { IDLE, LOADING, CASTING }

data class CastImageUiState(
    val tvName: String = "",
    val title: String = "",
    val imageUri: Uri? = null,
    val phase: CastImagePhase = CastImagePhase.IDLE,
)