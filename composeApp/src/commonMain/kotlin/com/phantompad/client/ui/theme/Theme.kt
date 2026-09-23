package com.phantompad.client.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// -- Color palette: dark gaming aesthetic --

val DarkBackground = Color(0xFF0D0D0F)
val DarkSurface = Color(0xFF1A1A1E)
val DarkSurfaceVariant = Color(0xFF2A2A30)
val DarkOnSurface = Color(0xFFE0E0E4)
val DarkOnSurfaceVariant = Color(0xFF8E8E96)

val AccentPurple = Color(0xFF9B6DFF)
val AccentPurpleVariant = Color(0xFF7B4FD4)
val AccentCyan = Color(0xFF4DD9E8)

// Xbox-style button colors
val ButtonA = Color(0xFF3DDC84) // Green
val ButtonB = Color(0xFFEA4335) // Red
val ButtonX = Color(0xFF4285F4) // Blue
val ButtonY = Color(0xFFFBBC04) // Yellow

val ButtonDefault = Color(0xFF3A3A42)
val ButtonPressed = Color(0xFF5A5A66)
val ButtonBorder = Color(0xFF4A4A52)

val StatusConnected = Color(0xFF3DDC84)
val StatusError = Color(0xFFEA4335)

private val PhantomPadDarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    onPrimary = Color.White,
    primaryContainer = AccentPurpleVariant,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = StatusError,
    onError = Color.White,
)

@Composable
fun PhantomPadTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PhantomPadDarkColorScheme,
        content = content,
    )
}
