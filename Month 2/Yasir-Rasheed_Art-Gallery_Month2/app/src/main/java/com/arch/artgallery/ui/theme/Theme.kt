package com.arch.artgallery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF14121F)
private val Violet = Color(0xFF6C4DFF)
private val Coral = Color(0xFFFF6B6B)

private val LightColors = lightColorScheme(
    primary = Violet,
    secondary = Coral,
    background = Color(0xFFF8F7FC),
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA796FF),
    secondary = Coral,
    background = Color(0xFF0F0D18),
    surface = Color(0xFF1A1727),
    onBackground = Color(0xFFF1EFFA),
    onSurface = Color(0xFFF1EFFA)
)

@Composable
fun ArtGalleryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
