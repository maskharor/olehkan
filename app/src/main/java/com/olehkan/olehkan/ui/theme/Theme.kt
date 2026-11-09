package com.olehkan.olehkan.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OlehKanMaterialShapes,
        content = content
    )
}