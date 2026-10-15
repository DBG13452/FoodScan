package com.foodscan.app.ui.additives

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.foodscan.app.R
import com.foodscan.app.data.additives.AdditiveConcern
import com.foodscan.app.data.additives.AdditiveDictionary
import com.foodscan.app.ui.theme.FoodScanColors
import com.foodscan.app.ui.theme.FoodScanSpacing

@Composable
internal fun AdditiveConcern.label(): String = stringResource(when (this) {
    AdditiveConcern.HIGH -> R.string.additive_concern_high
    AdditiveConcern.ATTENTION -> R.string.additive_concern_attention
    AdditiveConcern.LOW -> R.string.additive_concern_low
    AdditiveConcern.UNKNOWN -> R.string.additive_concern_unknown
})

@Composable
internal fun AdditiveConcern.foreground(): Color = when (this) {
    AdditiveConcern.HIGH -> MaterialTheme.colorScheme.error
    AdditiveConcern.ATTENTION -> FoodScanColors.warningText
    AdditiveConcern.LOW -> FoodScanColors.successText
    AdditiveConcern.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
internal fun AdditiveConcern.container(): Color = when (this) {
    AdditiveConcern.HIGH -> MaterialTheme.colorScheme.errorContainer
    AdditiveConcern.ATTENTION -> FoodScanColors.warningContainer
    AdditiveConcern.LOW -> FoodScanColors.successContainer
    AdditiveConcern.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
}

/** Color is always paired with text, including in the product's ingredients section. */
@Composable
fun AdditiveRow(code: String, onOpen: (() -> Unit)? = null) {
    val info = AdditiveDictionary.find(code)
    val concern = info?.concern ?: AdditiveConcern.UNKNOWN
    val heading = listOfNotNull(AdditiveDictionary.normalize(code), info?.name).joinToString("  ")
    val content: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().heightIn(min = FoodScanSpacing.buttonMinHeight)
            .padding(FoodScanSpacing.large).semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.extraSmall)) {
            Text(heading, style = MaterialTheme.typography.titleMedium)
            Text(concern.label(), color = concern.foreground(), style = MaterialTheme.typography.labelLarge)
        }
    }
    if (onOpen != null) Surface(onClick = onOpen, modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, color = concern.container(),
        border = BorderStroke(1.dp, concern.foreground().copy(alpha = .25f)), content = content)
    else Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        color = concern.container(), content = content)
}
