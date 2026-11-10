package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkMedicalBluePrimary,
    onPrimary = Color(0xFF00296B),
    primaryContainer = Color(0xFF003E9C),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = MedicalTealSecondary,
    onSecondary = Color.White,
    background = DarkMedicalBackground,
    onBackground = DarkMedicalText,
    surface = DarkMedicalSurface,
    onSurface = DarkMedicalText,
    surfaceVariant = DarkMedicalSurfaceVariant,
    onSurfaceVariant = DarkMedicalTextMuted,
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A)
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalBluePrimary,
    onPrimary = MedicalBlueOnPrimary,
    primaryContainer = MedicalBlueContainer,
    onPrimaryContainer = MedicalBlueOnContainer,
    secondary = MedicalTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = MedicalTealContainer,
    onSecondaryContainer = MedicalTealOnContainer,
    background = CleanBackground,
    onBackground = NavyTypography,
    surface = CleanSurface,
    onSurface = NavyTypography,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = NavyTypographyMuted,
    error = HealthError,
    errorContainer = HealthErrorContainer
)

@Composable
fun MediTimeTheme(
    darkTheme: Boolean = false, // Defaults strictly to false (Light Mode) as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
