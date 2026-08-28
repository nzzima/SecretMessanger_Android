package com.nzzima.secretmessanger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnBackground,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorColor,
)

/** Тема приложения. Светлой схемы нет: [ColorScheme] применяется при любых настройках системы. */
@Composable
fun SecretMessangerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, content = content)
}
