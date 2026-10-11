package com.foodscan.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.foodscan.app.R
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanCard
import com.foodscan.app.ui.components.FoodScanMessage
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanSectionTitle
import com.foodscan.app.ui.theme.FoodScanTheme
import com.foodscan.app.ui.theme.FoodScanSpacing

@Composable
fun HomeScreen(
    onScan: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
    recentProducts: List<HomeDemoProduct> = homeDemoProducts,
) {
    FoodScanScreenColumn(modifier) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        NutritionScenarioCard(onProfile = onProfile)
        FoodScanButton(
            text = stringResource(R.string.scan_product),
            onClick = onScan,
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
        )
        FoodScanSectionTitle(stringResource(R.string.recent_title))
        if (recentProducts.isEmpty()) {
            FoodScanMessage(
                title = stringResource(R.string.recent_empty_title),
                message = stringResource(R.string.recent_empty_message),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            recentProducts.forEach { product ->
                FoodScanCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(product.name), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(product.category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        FoodScanButton(
            text = stringResource(R.string.open_favorites),
            onClick = onFavorites,
            modifier = Modifier.fillMaxWidth(),
            style = FoodScanButtonStyle.Secondary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NutritionScenarioCard(onProfile: () -> Unit) {
    FoodScanCard(Modifier.fillMaxWidth()) {
        // Keep one compact row when possible; wrap instead of clipping at large font sizes.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.extraSmall),
        ) {
            Text(
                text = stringResource(R.string.scenario_normal),
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            TextButton(
                onClick = onProfile,
                modifier = Modifier.heightIn(min = FoodScanSpacing.buttonMinHeight),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(FoodScanSpacing.small),
            ) {
                Text(stringResource(R.string.open_profile))
            }
        }
    }
}

@Preview(name = "Home • phone", widthDp = 360, heightDp = 840, locale = "ru")
@Preview(name = "Home • narrow • 200%", widthDp = 320, heightDp = 840, fontScale = 2f, locale = "ru")
@Preview(name = "Home • tablet", widthDp = 800, heightDp = 1100, locale = "ru")
@Composable
private fun HomeScreenPreview() {
    FoodScanTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(onScan = {}, onFavorites = {}, onProfile = {})
        }
    }
}

@Preview(name = "Home • empty", widthDp = 360, heightDp = 1000, locale = "ru")
@Composable
private fun EmptyHomeScreenPreview() {
    FoodScanTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(onScan = {}, onFavorites = {}, onProfile = {}, recentProducts = emptyList())
        }
    }
}
