package com.foodscan.app.ui.theme

import androidx.compose.ui.graphics.Color

/** Brand tokens; screens use MaterialTheme.colorScheme and FoodScanColors. */
object FoodScanPalette {
    val BrandPrimary = Color(0xFF0B3D32)
    val BrandAccent = Color(0xFF35C759)
    val BrandSoft = Color(0xFFE8F7EE)
    val Background = Color(0xFFFAFBF8)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF17201D)
    val TextSecondary = Color(0xFF66736D)
    val Border = Color(0xFFE2E9E5)
    val Success = Color(0xFF22A06B)
    val Warning = Color(0xFFF2B84B)
    val Danger = Color(0xFFE05252)

    // Accessible foreground / container extensions; brand colors stay unchanged.
    val ErrorText = Color(0xFFA12626)
    val ErrorContainer = Color(0xFFFDECEC)
    val WarningContainer = Color(0xFFFFF5DE)
    val InverseText = Color(0xFFF1F5F2)
}

/** Material 3 has no success / warning roles. */
object FoodScanColors {
    val success = FoodScanPalette.Success
    val successContainer = FoodScanPalette.BrandSoft
    val warning = FoodScanPalette.Warning
    val warningContainer = FoodScanPalette.WarningContainer
    val danger = FoodScanPalette.Danger
    val dangerContainer = FoodScanPalette.ErrorContainer
}