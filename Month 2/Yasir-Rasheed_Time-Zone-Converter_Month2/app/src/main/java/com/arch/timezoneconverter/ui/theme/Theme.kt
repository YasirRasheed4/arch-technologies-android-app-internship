package com.arch.timezoneconverter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.arch.timezoneconverter.logic.Availability

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    secondary = Color(0xFF3B5BDB),
    background = Color(0xFFF4F8F8),
    surface = Color.White,
    onBackground = Color(0xFF10201F),
    onSurface = Color(0xFF10201F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5EEAD4),
    secondary = Color(0xFF91A7FF),
    background = Color(0xFF0B1514),
    surface = Color(0xFF14201F),
    onBackground = Color(0xFFE6F2F1),
    onSurface = Color(0xFFE6F2F1)
)

@Composable
fun TimeZoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}

fun availabilityColor(a: Availability): Color = when (a) {
    Availability.WORKING -> Color(0xFF2E9E6B)
    Availability.EDGE -> Color(0xFFF2B33D)
    Availability.NIGHT -> Color(0xFF5B6078)
}

fun availabilityText(a: Availability): String = when (a) {
    Availability.WORKING -> "Working hours"
    Availability.EDGE -> "Early / late"
    Availability.NIGHT -> "Night"
}
