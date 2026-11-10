package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// MediTime Professional Color Design System
// ==========================================

// Primary Palette
val RoyalBlue = Color(0xFF2457D6)
val RoyalBlueDark = Color(0xFF1B41A8)
val DeepNavy = Color(0xFF142B4D)
val DeepNavyDark = Color(0xFF0B172A)
val MediTeal = Color(0xFF13B8A6)
val MediTealDark = Color(0xFF0D9488)
val SoftBlue = Color(0xFFE9F4FF)
val SoftBlueAlt = Color(0xFFD8ECFF)
val LightBackground = Color(0xFFF4F7FB)
val PureWhite = Color(0xFFFFFFFF)

// Healthcare Status Colors
val StatusTaken = Color(0xFF16A34A)
val StatusTakenContainer = Color(0xFFDCFCE7)
val StatusUpcoming = Color(0xFFF59E0B)
val StatusUpcomingContainer = Color(0xFFFEF3C7)
val StatusMissed = Color(0xFFE5484D)
val StatusMissedContainer = Color(0xFFFFE2E2)
val StatusSkipped = Color(0xFF64748B)
val StatusSkippedContainer = Color(0xFFF1F5F9)

// Backward Compatibility Aliases
val MedicalBluePrimary = RoyalBlue
val MedicalBlueOnPrimary = PureWhite
val MedicalBlueContainer = SoftBlue
val MedicalBlueOnContainer = DeepNavy

val MedicalTealSecondary = MediTeal
val MedicalTealContainer = Color(0xFFCCFBF1)
val MedicalTealOnContainer = Color(0xFF115E59)

val MedicalAccent = RoyalBlue
val HealthSuccess = StatusTaken
val HealthSuccessContainer = StatusTakenContainer
val HealthWarning = StatusUpcoming
val HealthWarningContainer = StatusUpcomingContainer
val HealthError = StatusMissed
val HealthErrorContainer = StatusMissedContainer

// Neutral Typography & Surfaces
val CleanBackground = LightBackground
val CleanSurface = PureWhite
val CleanSurfaceVariant = Color(0xFFEDF2F7)
val NavyTypography = DeepNavy
val NavyTypographyMuted = Color(0xFF64748B)
val BorderSubtle = Color(0xFFE2E8F0)

// Dark Theme Palette
val DarkMedicalBluePrimary = Color(0xFF7098FF)
val DarkMedicalBlueContainer = Color(0xFF1B3B82)
val DarkMedicalBackground = Color(0xFF0B1320)
val DarkMedicalSurface = Color(0xFF131F33)
val DarkMedicalSurfaceVariant = Color(0xFF1C2B44)
val DarkMedicalText = Color(0xFFF1F5F9)
val DarkMedicalTextMuted = Color(0xFF94A3B8)
val DarkBorderSubtle = Color(0xFF263750)

// Gradients
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(RoyalBlue, Color(0xFF1D4ED8), DeepNavy)
)

val HeroCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2154CF), Color(0xFF1A3E99), DeepNavy)
)

val HeroCardGradientSenior = Brush.linearGradient(
    colors = listOf(Color(0xFFE0EDFF), Color(0xFFD0E4FF), SoftBlue)
)

val SoftBlueGradient = Brush.verticalGradient(
    colors = listOf(SoftBlue, LightBackground)
)

val SuccessGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF16A34A), Color(0xFF15803D))
)
