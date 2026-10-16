package com.foodscan.app.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.foodscan.app.R
import com.foodscan.app.ui.additives.AdditiveRow
import com.foodscan.app.data.product.DemoProductRepository
import com.foodscan.app.domain.model.NutritionBasis
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductSource
import com.foodscan.app.domain.model.NutritionScenario
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanBackButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanCard
import com.foodscan.app.ui.components.FoodScanEmptyState
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanScreenTitle
import com.foodscan.app.ui.components.FoodScanSectionTitle
import com.foodscan.app.ui.theme.FoodScanSpacing
import com.foodscan.app.ui.theme.FoodScanTheme

@Composable
fun ProductScreen(
    product: Product?, onBack: () -> Unit, modifier: Modifier = Modifier,
    scenario: NutritionScenario = NutritionScenario.NORMAL,
    isFavorite: Boolean = false, favoriteBusy: Boolean = false,
    onFavorite: (() -> Unit)? = null,
    onAdditive: ((String) -> Unit)? = null,
    onMethodology: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    FoodScanScreenColumn(modifier.testTag("product-screen")) {
        FoodScanBackButton(onBack)
        if (product == null) {
            FoodScanEmptyState(
                R.drawable.ic_scan,
                stringResource(R.string.product_not_found),
                stringResource(R.string.product_not_found_message),
            )
        } else {
            FoodScanScreenTitle(product.name, product.category)
            if (onFavorite != null) FoodScanButton(
                stringResource(if (isFavorite) R.string.favorite_remove else R.string.favorite_add),
                onFavorite, Modifier.fillMaxWidth(), style = FoodScanButtonStyle.Secondary, enabled = !favoriteBusy,
                leadingIcon = {
                    Icon(painterResource(if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite),
                        null, Modifier.size(24.dp),
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else LocalContentColor.current)
                },
            )
            if (product.imageUrl != null) {
                SubcomposeAsyncImage(
                    model = product.imageUrl,
                    contentDescription = stringResource(R.string.product_photo),
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Fit,
                    loading = { Text(stringResource(R.string.product_photo_loading)) },
                    error = { Text(stringResource(R.string.product_photo_error)) },
                    success = { SubcomposeAsyncImageContent() },
                )
            } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FoodScanSpacing.medium),
            ) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(painterResource(R.drawable.ic_product), null, Modifier.padding(16.dp).size(32.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
                Text(stringResource(R.string.product_no_photo), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            }
            ProductScoreCard(product, scenario, onMethodology)
            FoodScanCard(Modifier.fillMaxWidth()) {
                if (product.source == ProductSource.OPEN_FOOD_FACTS) {
                    ProductField(stringResource(R.string.product_brands), product.brands.orNoData())
                }
                ProductField(stringResource(R.string.product_manufacturer), product.manufacturer.orNoData())
                ProductField(stringResource(R.string.product_barcode), product.barcode.orNoData())
                ProductField(stringResource(R.string.product_source), stringResource(when (product.source) {
                    ProductSource.DEMO -> R.string.product_source_demo
                    ProductSource.OPEN_FOOD_FACTS -> R.string.product_source_off
                }))
                Text(stringResource(if (product.source == ProductSource.DEMO) R.string.product_demo_notice
                    else R.string.product_off_notice), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (product.source == ProductSource.OPEN_FOOD_FACTS) {
                    FoodScanButton(stringResource(R.string.product_open_source), {
                        uriHandler.openUri("https://world.openfoodfacts.org/product/${product.id}")
                    }, style = FoodScanButtonStyle.Text)
                    Text(stringResource(R.string.product_off_license), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.medium)) {
                FoodScanSectionTitle(stringResource(R.string.product_nutrition))
                Text(stringResource(when (product.nutrition.basis) {
                    NutritionBasis.PER_100_G -> R.string.product_per_100_g
                    NutritionBasis.PER_100_ML -> R.string.product_per_100_ml
                    NutritionBasis.PER_100_G_OR_ML -> R.string.product_per_100_g_or_ml
                }), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FoodScanCard(Modifier.fillMaxWidth()) {
                    val nutrition = product.nutrition
                    NutrientRow(R.string.product_energy, nutrition.energyKcal, R.string.product_kcal, "energy")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NutrientRow(R.string.product_protein, nutrition.proteinGrams, tag = "protein")
                    NutrientRow(R.string.product_fat, nutrition.fatGrams, tag = "fat")
                    NutrientRow(R.string.product_carbohydrate, nutrition.carbohydrateGrams, tag = "carbohydrate")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NutrientRow(R.string.product_sugar, nutrition.sugarGrams, tag = "sugar")
                    NutrientRow(R.string.product_salt, nutrition.saltGrams, tag = "salt")
                    NutrientRow(R.string.product_saturated_fat, nutrition.saturatedFatGrams, tag = "saturated-fat")
                    NutrientRow(R.string.product_fiber, nutrition.fiberGrams, tag = "fiber")
                }
            }
            ProductSection(stringResource(R.string.product_ingredients), product.ingredients.orNoData())
            ProductAllergens(product)
            if (!product.additives.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.small)) {
                    FoodScanSectionTitle(stringResource(R.string.product_additives))
                    product.additives.forEach { code ->
                        AdditiveRow(code, onAdditive?.let { open -> { open(code) } })
                    }
                }
            } else ProductSection(stringResource(R.string.product_additives), product.additives.asSourceList())
        }
    }
}

@Composable
private fun String?.orNoData(): String = takeUnless { it.isNullOrBlank() }
    ?: stringResource(R.string.product_no_data)

@Composable
private fun List<String>?.asSourceList(): String = when {
    this == null -> stringResource(R.string.product_no_data)
    isEmpty() -> stringResource(R.string.product_not_declared)
    else -> joinToString(", ")
}

@Composable
private fun ProductSection(title: String, text: String) {
    FoodScanCard(Modifier.fillMaxWidth()) {
        FoodScanSectionTitle(title)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductField(label: String, value: String, modifier: Modifier = Modifier, emphasized: Boolean = false) {
    // Values wrap onto the next line instead of fighting the label for fixed columns.
    FlowRow(
        modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(FoodScanSpacing.large, Alignment.Start),
        verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.extraSmall),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun NutrientRow(label: Int, value: Double?, unit: Int = R.string.product_grams, tag: String) {
    ProductField(
        stringResource(label),
        formatNutrient(value, stringResource(unit), stringResource(R.string.product_no_data)),
        Modifier.padding(vertical = FoodScanSpacing.extraSmall).testTag("nutrition-$tag"),
        emphasized = value != null,
    )
}

@Preview(name = "Product", widthDp = 360, heightDp = 840, locale = "ru")
@Preview(name = "Product • 200%", widthDp = 320, heightDp = 840, fontScale = 2f, locale = "ru")
@Preview(name = "Product • tablet", widthDp = 800, heightDp = 1100, locale = "ru")
@Composable
private fun ProductPreview() {
    FoodScanTheme { ProductScreen(DemoProductRepository.products.first(), {}) }
}

@Preview(name = "Product • missing data", widthDp = 360, heightDp = 840, locale = "ru")
@Composable
private fun MissingProductDataPreview() {
    FoodScanTheme { ProductScreen(DemoProductRepository.products.last(), {}) }
}

@Preview(name = "Product • not found", widthDp = 320, heightDp = 840, fontScale = 2f, locale = "ru")
@Composable
private fun ProductNotFoundPreview() {
    FoodScanTheme { ProductScreen(null, {}) }
}
