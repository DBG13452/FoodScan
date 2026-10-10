package com.foodscan.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.foodscan.app.ui.theme.FoodScanSpacing

enum class FoodScanButtonStyle { Primary, Secondary, Text }

/** Height grows with the label / system font. Width belongs to the caller. */
@Composable
fun FoodScanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: FoodScanButtonStyle = FoodScanButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val buttonModifier = modifier.heightIn(min = FoodScanSpacing.buttonMinHeight)
    val padding = PaddingValues(
        horizontal = FoodScanSpacing.extraLarge,
        vertical = FoodScanSpacing.medium,
    )
    val label: @Composable () -> Unit = {
        Text(text = text, textAlign = TextAlign.Center)
    }
    when (style) {
        FoodScanButtonStyle.Primary -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            contentPadding = padding,
        ) { label() }
        FoodScanButtonStyle.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            contentPadding = padding,
        ) { label() }
        FoodScanButtonStyle.Text -> TextButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            contentPadding = padding,
        ) { label() }
    }
}