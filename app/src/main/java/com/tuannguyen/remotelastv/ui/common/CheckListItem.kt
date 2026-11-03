package com.tuannguyen.remotelastv.ui.common

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class ChecklistItem(
    val icon: ImageVector,
    @StringRes val title: Int,
    @StringRes val body: Int,
)