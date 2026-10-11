package com.foodscan.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.foodscan.app.R
import com.foodscan.app.ui.components.FoodScanBarcode
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanCard
import com.foodscan.app.ui.components.FoodScanEmptyState
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanScreenTitle
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
        FoodScanScreenTitle(stringResource(R.string.app_name), stringResource(R.string.home_purpose))
        Surface(
            onClick = onScan,
            modifier = Modifier.fillMaxWidth().semantics { role = Role.Button },
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Column(Modifier.padding(FoodScanSpacing.extraLarge),
                verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.extraLarge)) {
                FoodScanBarcode(Modifier.padding(2.dp).width(136.dp).height(64.dp))
                Text(stringResource(R.string.scan_product), style = MaterialTheme.typography.headlineSmall)
            }
        }
        NutritionScenarioCard(onProfile)
        Column(verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.medium)) {
            FoodScanSectionTitle(stringResource(R.string.recent_title))
            if (recentProducts.isEmpty()) {
                FoodScanEmptyState(R.drawable.ic_scan, stringResource(R.string.recent_empty_title),
                    stringResource(R.string.recent_empty_message))
            } else {
                FoodScanCard(Modifier.fillMaxWidth()) {
                    recentProducts.forEachIndexed { index, product ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(Modifier.fillMaxWidth().padding(vertical = FoodScanSpacing.small),
                            verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.extraSmall)) {
                            Text(stringResource(product.name), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(product.category), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        FoodScanButton(stringResource(R.string.open_favorites), onFavorites, Modifier.fillMaxWidth(),
            style = FoodScanButtonStyle.Secondary)
    }
}

@Composable
private fun NutritionScenarioCard(onProfile: () -> Unit) {
    Surface(onClick = onProfile, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)
        .semantics { role = Role.Button }, shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
        Row(Modifier.padding(FoodScanSpacing.large), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FoodScanSpacing.medium)) {
            Icon(painterResource(R.drawable.ic_profile), null, Modifier.size(24.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.scenario_title), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.scenario_normal), style = MaterialTheme.typography.titleMedium)
            }
            Icon(painterResource(R.drawable.ic_chevron_right), stringResource(R.string.open_profile), Modifier.size(24.dp))
        }
    }
}

@Preview(name = "Home • phone", widthDp = 360, heightDp = 840, locale = "ru")
@Preview(name = "Home • narrow • 200%", widthDp = 320, heightDp = 840, fontScale = 2f, locale = "ru")
@Preview(name = "Home • tablet", widthDp = 800, heightDp = 1100, locale = "ru")
@Composable
private fun HomeScreenPreview() {
    FoodScanTheme { Surface { HomeScreen({}, {}, {}) } }
}

@Preview(name = "Home • empty", widthDp = 360, heightDp = 1000, locale = "ru")
@Composable
private fun EmptyHomeScreenPreview() {
    FoodScanTheme { Surface { HomeScreen({}, {}, {}, recentProducts = emptyList()) } }
}
