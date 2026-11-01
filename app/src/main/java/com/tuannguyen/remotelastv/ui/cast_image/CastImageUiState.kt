package com.tuannguyen.remotelastv.ui.cast_image

import android.net.Uri
import com.tuannguyen.remotelastv.domain.model.CastImagePhase

data class CastImageUiState(
    val tvName: String = "",
    val title: String = "",
    val imageUri: Uri? = null,
    val phase: CastImagePhase = CastImagePhase.IDLE,
)