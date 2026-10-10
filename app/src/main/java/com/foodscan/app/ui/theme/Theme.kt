package com.foodscan.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FoodScanLightColorScheme = lightColorScheme(
    primary = FoodScanPalette.BrandPrimary,
    onPrimary = FoodScanPalette.Surface,
    primaryContainer = FoodScanPalette.BrandSoft,
    onPrimaryContainer = FoodScanPalette.BrandPrimary,
    inversePrimary = FoodScanPalette.BrandAccent,
    secondary = FoodScanPalette.BrandPrimary,
    onSecondary = FoodScanPalette.Surface,
    secondaryContainer = FoodScanPalette.BrandSoft,
    onSecondaryContainer = FoodScanPalette.BrandPrimary,
    tertiary = FoodScanPalette.BrandAccent,
    onTertiary = FoodScanPalette.TextPrimary,
    tertiaryContainer = FoodScanPalette.BrandSoft,
    onTertiaryContainer = FoodScanPalette.BrandPrimary,
    background = FoodScanPalette.Background,
    onBackground = FoodScanPalette.TextPrimary,
    surface = FoodScanPalette.Surface,
    onSurface = FoodScanPalette.TextPrimary,
    surfaceVariant = FoodScanPalette.Background,
    onSurfaceVariant = FoodScanPalette.TextSecondary,
    surfaceTint = FoodScanPalette.BrandPrimary,
    inverseSurface = FoodScanPalette.TextPrimary,
    inverseOnSurface = FoodScanPalette.InverseText,
    error = FoodScanPalette.ErrorText,
    onError = FoodScanPalette.Surface,
    errorContainer = FoodScanPalette.ErrorContainer,
    onErrorContainer = FoodScanPalette.ErrorText,
    outline = FoodScanPalette.TextSecondary,
    outlineVariant = FoodScanPalette.Border,
    scrim = FoodScanPalette.TextPrimary,
    surfaceBright = FoodScanPalette.Surface,
    surfaceDim = FoodScanPalette.Border,
    surfaceContainerLowest = FoodScanPalette.Surface,
    surfaceContainerLow = FoodScanPalette.Background,
    surfaceContainer = FoodScanPalette.Background,
    surfaceContainerHigh = FoodScanPalette.BrandSoft,
    surfaceContainerHighest = FoodScanPalette.Border,
)

/** Stage 2 deliberately uses the light brand theme regardless of system colors. */
@Composable
fun FoodScanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FoodScanLightColorScheme,
        typography = FoodScanTypography,
        shapes = FoodScanShapes,
        content = content,
    )
}
