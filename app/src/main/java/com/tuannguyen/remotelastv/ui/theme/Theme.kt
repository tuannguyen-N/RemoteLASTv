package com.tuannguyen.remotelastv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = Indigo500,                // nút/tab đang chọn
    onPrimary = White,                  // chữ trên primary
    primaryContainer = IndigoDeep,      // nền nút micro
    onPrimaryContainer = Indigo200,     // icon trên nút micro

    background = Ink900,                // nền màn hình
    onBackground = Mist100,             // chữ trên nền màn hình

    surface = Ink900,
    onSurface = Mist100,                // chữ chính
    onSurfaceVariant = Mist300,         // chữ phụ, icon mờ
    surfaceContainerLow = Ink800,       // khối lớn
    surfaceContainer = Ink700,          // nút nhỏ

    outline = Ink500,                   // viền

    error = Red500,                     // icon nút nguồn
    errorContainer = RedDeep,           // nền nút nguồn
)

@Immutable
data class ExtendedColors(
    val success: Color,
    val link: Color,
)

private val AppExtendedColors = ExtendedColors(
    success = Green400,
    link = Indigo200,
)

private val LocalExtendedColors = staticCompositionLocalOf { AppExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

@Composable
fun RemoteLASTvTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalExtendedColors provides AppExtendedColors) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}