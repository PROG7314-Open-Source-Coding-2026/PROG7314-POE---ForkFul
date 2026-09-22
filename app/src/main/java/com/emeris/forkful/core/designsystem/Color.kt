package com.emeris.forkful.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ForestGreen = Color(0xFF085434)
val ForestGreenDark = Color(0xFF063F27)
val ForestGreenLight = Color(0xFF0F7A4D)
val MintLight = Color(0xFFE4F3EB)
val MintSubtle = Color(0xFFEDF7F1)
val TrueWhite = Color(0xFFFFFFFF)
val AlertRed = Color(0xFFBA1A1A)
val AlertPinkBackground = Color(0xFFFFDAD6)
val AlertPinkText = Color(0xFF93000A)
val ChipSelectedGreen = Color(0xFF085434)
val ChipUnselectedCream = Color(0xFFECE7DC)
val TextSubtle = Color(0xFF8B948E)
val DarkAuthBackground = Color(0xFF131512)
val DarkAuthBorder = Color(0xFF2B312C)

val CreamBackground: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF121412) else Color(0xFFFAF7F0)

val CreamSurface: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF1E2320) else Color(0xFFF4EFE6)

val PureWhite: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF1C201D) else Color(0xFFFFFFFF)

val TextCharcoal: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFFEDEAE4) else Color(0xFF191C1A)

val TextMuted: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF9EAAA2) else Color(0xFF5F6862)

val BorderLight: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF2C332E) else Color(0xFFE0DDD5)