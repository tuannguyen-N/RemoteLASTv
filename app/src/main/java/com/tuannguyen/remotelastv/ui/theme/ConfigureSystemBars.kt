package com.tuannguyen.remotelastv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.tuannguyen.remotelastv.ui.utils.findActivity

@Composable
fun ConfigureSystemBars() {
    val view = LocalView.current
    val backgroundColor = MaterialTheme.colorScheme.background

    val window = remember(view) {
        view.context.findActivity()?.window
    }

    SideEffect {
        window?.let { win ->
            win.statusBarColor = backgroundColor.toArgb()
            win.navigationBarColor = backgroundColor.toArgb()

            WindowCompat.getInsetsController(win, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
}