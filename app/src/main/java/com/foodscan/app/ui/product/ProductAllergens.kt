package com.foodscan.app.ui.product

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.foodscan.app.R
import com.foodscan.app.domain.allergens.AllergenAnalyzer
import com.foodscan.app.domain.model.Product
import com.foodscan.app.ui.components.FoodScanSectionTitle
import com.foodscan.app.ui.components.FoodScanCard

@Composable
internal fun ProductAllergens(product: Product) {
    // Recompute for old saved snapshots too; retain the raw API fields in storage.
    val summary = remember(product) { AllergenAnalyzer.analyze(product) }
    FoodScanCard(Modifier.fillMaxWidth().testTag("product-allergens")) {
        FoodScanSectionTitle(stringResource(R.string.product_allergens))
        if (summary.contains.isEmpty() && summary.traces.isEmpty()) {
            Text(stringResource(R.string.allergens_no_verified_data), style = MaterialTheme.typography.bodyLarge)
        }
        if (summary.contains.isNotEmpty()) Text(summary.contains.joinToString(", ") { it.name },
            style = MaterialTheme.typography.bodyLarge)
        if (summary.traces.isNotEmpty()) Text(stringResource(R.string.allergens_possible_traces,
            summary.traces.joinToString(", ") { it.name }), style = MaterialTheme.typography.bodyLarge)
        if (summary.conflicts.isNotEmpty()) Text(stringResource(R.string.allergens_conflict, summary.conflicts.joinToString(", ")),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        Text(stringResource(R.string.allergens_analysis_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

