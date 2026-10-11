package com.tuannguyen.remotelastv.ui.common

import androidx.compose.ui.graphics.vector.ImageVector

sealed interface IconSource {
    data class Vector(val imageVector: ImageVector): IconSource
    data class Resource(val resId: Int): IconSource
}