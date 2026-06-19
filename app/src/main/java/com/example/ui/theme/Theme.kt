package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CineOrangeColorScheme = darkColorScheme(
    primary = OrangePrimary,
    secondary = OrangeSecondary,
    background = BlackBackground,
    surface = BlackSurface,
    surfaceVariant = BlackSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Color(0xFFE50914) // Classic error red
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CineOrangeColorScheme,
        typography = Typography,
        content = content
    )
}
