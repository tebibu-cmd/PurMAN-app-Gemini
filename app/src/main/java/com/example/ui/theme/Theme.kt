package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PurManDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = PremiumBlack,
    secondary = NeonGreenSecondary,
    onSecondary = CleanWhite,
    tertiary = MutedSlate,
    onTertiary = PremiumBlack,
    background = PremiumBlack,
    onBackground = CleanWhite,
    surface = PremiumDarkCharcoal,
    onSurface = SoftWhite,
    surfaceVariant = PremiumCardGray,
    onSurfaceVariant = SoftWhite,
    error = ErrorRed,
    onError = PremiumBlack,
    outline = PremiumBorderGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the premium black/green vibe
    dynamicColor: Boolean = false, // Disable dynamic colors to keep the neon green accent brand perfect
    content: @Composable () -> Unit,
) {
    // We strictly use PurManDarkColorScheme to ensure the design style meets the explicit user prompt of premium dark black/neon green brand color scheme.
    val colorScheme = PurManDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

