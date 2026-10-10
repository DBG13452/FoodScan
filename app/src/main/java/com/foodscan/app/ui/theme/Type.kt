package com.foodscan.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun foodScanTextStyle(size: Int, height: Int, weight: FontWeight) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = 0.sp,
)

val FoodScanTypography = Typography(
    displayLarge = foodScanTextStyle(57, 64, FontWeight.Normal),
    displayMedium = foodScanTextStyle(45, 52, FontWeight.Normal),
    displaySmall = foodScanTextStyle(36, 44, FontWeight.Normal),
    headlineLarge = foodScanTextStyle(32, 40, FontWeight.SemiBold),
    headlineMedium = foodScanTextStyle(28, 36, FontWeight.SemiBold),
    headlineSmall = foodScanTextStyle(24, 32, FontWeight.SemiBold),
    titleLarge = foodScanTextStyle(22, 28, FontWeight.SemiBold),
    titleMedium = foodScanTextStyle(16, 24, FontWeight.SemiBold),
    titleSmall = foodScanTextStyle(14, 20, FontWeight.SemiBold),
    bodyLarge = foodScanTextStyle(16, 24, FontWeight.Normal),
    bodyMedium = foodScanTextStyle(14, 20, FontWeight.Normal),
    bodySmall = foodScanTextStyle(12, 16, FontWeight.Normal),
    labelLarge = foodScanTextStyle(14, 20, FontWeight.SemiBold),
    labelMedium = foodScanTextStyle(12, 16, FontWeight.Medium),
    labelSmall = foodScanTextStyle(11, 16, FontWeight.Medium),
)