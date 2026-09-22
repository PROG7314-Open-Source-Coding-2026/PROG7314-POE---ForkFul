package com.emeris.forkful.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF085434),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4F3EB),
    onPrimaryContainer = Color(0xFF063F27),
    background = Color(0xFFFAF7F0),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF191C1A),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFF4EFE6),
    onSurfaceVariant = Color(0xFF5F6862),
    outline = Color(0xFFE0DDD5)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2FA36B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F3824),
    onPrimaryContainer = Color(0xFFC7EED8),
    background = Color(0xFF121412),
    surface = Color(0xFF1C201D),
    onBackground = Color(0xFFEDEAE4),
    onSurface = Color(0xFFEDEAE4),
    surfaceVariant = Color(0xFF232824),
    onSurfaceVariant = Color(0xFF9EAAA2),
    outline = Color(0xFF2C332E)
)

@Composable
fun ForkfulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ForkfulTypography,
            content = content
        )
    }
}