package com.foodscan.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.foodscan.app.ui.theme.FoodScanColors
import com.foodscan.app.ui.theme.FoodScanSpacing

enum class FoodScanMessageTone { Info, Success, Warning, Error }

/** Title must explain the state in words: color alone does not convey meaning. */
@Composable
fun FoodScanMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    tone: FoodScanMessageTone = FoodScanMessageTone.Info,
) {
    val colors = MaterialTheme.colorScheme
    val (container, accent) = when (tone) {
        FoodScanMessageTone.Info -> colors.primaryContainer to colors.primary
        FoodScanMessageTone.Success -> FoodScanColors.successContainer to FoodScanColors.success
        FoodScanMessageTone.Warning -> FoodScanColors.warningContainer to FoodScanColors.warning
        FoodScanMessageTone.Error -> FoodScanColors.dangerContainer to FoodScanColors.danger
    }
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, accent),
    ) {
        Column(
            modifier = Modifier.padding(FoodScanSpacing.large),
            verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.extraSmall),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}