package com.foodscan.app.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
        Text(
            text = stringResource(R.string.home_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FoodScanCard(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.scenario_title), style = MaterialTheme.typography.labelLarge)
            Text(
                stringResource(R.string.scenario_normal),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(R.string.scenario_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FoodScanButton(
                text = stringResource(R.string.open_profile),
                onClick = onProfile,
                style = FoodScanButtonStyle.Text,
            )
        }
        FoodScanButton(
            text = stringResource(R.string.scan_product),
            onClick = onScan,
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
        )
        Text(
            stringResource(R.string.scan_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FoodScanSectionTitle(stringResource(R.string.recent_title))
        if (recentProducts.isEmpty()) {
            FoodScanMessage(
                title = stringResource(R.string.recent_empty_title),
                message = stringResource(R.string.recent_empty_message),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                stringResource(R.string.recent_demo_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            recentProducts.forEach { product ->
                FoodScanCard(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.demo_product_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
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
