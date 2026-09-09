package com.afrimedia.crm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Afri Media Interactive brand palette, matching the CRM plugin's admin/PDF styling.
val AmiOrange = Color(0xFFF2A71B)
val AmiBlack = Color(0xFF161616)
val AmiCream = Color(0xFFF7F3EA)
val AmiGreen = Color(0xFF2E9E5B)
val AmiRed = Color(0xFFD1453B)

private val LightColors = lightColorScheme(
    primary = AmiOrange,
    onPrimary = AmiBlack,
    secondary = AmiBlack,
    onSecondary = Color.White,
    background = AmiCream,
    surface = Color.White,
    onBackground = AmiBlack,
    onSurface = AmiBlack,
    error = AmiRed
)

private val DarkColors = darkColorScheme(
    primary = AmiOrange,
    onPrimary = AmiBlack,
    secondary = AmiOrange,
    onSecondary = AmiBlack,
    background = Color(0xFF121212),
    surface = Color(0xFF1D1D1D),
    onBackground = Color.White,
    onSurface = Color.White,
    error = AmiRed
)

@Composable
fun AfriMediaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
