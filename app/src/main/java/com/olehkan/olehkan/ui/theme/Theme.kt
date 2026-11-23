package com.olehkan.olehkan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val OlehKanLightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark,

    secondary = Orange,
    onSecondary = White,

    background = Paper,
    onBackground = Ink,

    surface = Paper,
    onSurface = Ink,
    surfaceVariant = CardBeige,
    onSurfaceVariant = Muted,

    outline = Line,
    outlineVariant = Line,

    error = HeartRed,
    onError = White,
)

@Composable
fun OlehKanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = OlehKanLightColorScheme


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OlehKanMaterialShapes,
        content = content
    )
}