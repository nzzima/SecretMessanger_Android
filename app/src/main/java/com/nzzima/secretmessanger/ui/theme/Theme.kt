package com.nzzima.secretmessanger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = BgMain,
    background = BgMain,
    onBackground = Ink,
    surface = Raised,
    onSurface = Ink,
    onSurfaceVariant = InkDim,
    surfaceVariant = TabBar,
    error = ErrorColor,
)

/** Тема приложения. Светлой схемы нет: [ColorScheme] применяется при любых настройках системы. */
@Composable
fun SecretMessangerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, content = content)
}
