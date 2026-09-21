package com.emeris.forkful.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = PureWhite,
    primaryContainer = MintLight,
    onPrimaryContainer = ForestGreenDark,
    background = CreamBackground,
    surface = PureWhite,
    onBackground = TextCharcoal,
    onSurface = TextCharcoal,
    surfaceVariant = CreamSurface,
    onSurfaceVariant = TextMuted,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = MintLight,
    onPrimary = ForestGreenDark,
    background = DarkAuthBackground,
    surface = DarkAuthBackground,
    onBackground = PureWhite,
    onSurface = PureWhite,
    outline = DarkAuthBorder
)

@Composable
fun ForkfulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ForkfulTypography,
        content = content
    )
}