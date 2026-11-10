package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkMedicalBluePrimary,
    onPrimary = DeepNavyDark,
    primaryContainer = DarkMedicalBlueContainer,
    onPrimaryContainer = Color(0xFFDCE7FF),
    secondary = MediTeal,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF0F4D45),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = StatusUpcoming,
    background = DarkMedicalBackground,
    onBackground = DarkMedicalText,
    surface = DarkMedicalSurface,
    onSurface = DarkMedicalText,
    surfaceVariant = DarkMedicalSurfaceVariant,
    onSurfaceVariant = DarkMedicalTextMuted,
    outline = DarkBorderSubtle,
    error = StatusMissed,
    errorContainer = Color(0xFF5A1517),
    onError = PureWhite,
    onErrorContainer = Color(0xFFFFD1D1)
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = PureWhite,
    primaryContainer = SoftBlue,
    onPrimaryContainer = DeepNavy,
    secondary = MediTeal,
    onSecondary = PureWhite,
    secondaryContainer = MedicalTealContainer,
    onSecondaryContainer = MedicalTealOnContainer,
    tertiary = StatusUpcoming,
    background = LightBackground,
    onBackground = DeepNavy,
    surface = CleanSurface,
    onSurface = DeepNavy,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = NavyTypographyMuted,
    outline = BorderSubtle,
    error = StatusMissed,
    errorContainer = StatusMissedContainer,
    onError = PureWhite,
    onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun MediTimeTheme(
    darkTheme: Boolean = false, // Defaults to Light Mode as per user specification
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
