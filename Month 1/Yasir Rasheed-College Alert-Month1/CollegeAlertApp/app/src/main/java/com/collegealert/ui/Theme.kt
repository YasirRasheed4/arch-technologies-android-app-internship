package com.collegealert.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(
    primary = Color(0xFF1E3A8A),
    onPrimary = Color.White,
    secondary = Color(0xFFF59E0B),
    background = Color(0xFFF8FAFC),
    surface = Color.White
)

@Composable
fun CollegeAlertTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
