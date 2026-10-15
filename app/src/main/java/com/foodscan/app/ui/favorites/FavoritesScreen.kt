package com.foodscan.app.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import com.foodscan.app.R
import com.foodscan.app.domain.model.Product
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanEmptyState
import com.foodscan.app.ui.components.FoodScanScreenTitle
import com.foodscan.app.ui.library.LibraryProductRow
import com.foodscan.app.ui.theme.FoodScanTheme
import com.foodscan.app.ui.theme.FoodScanSpacing

@Composable
fun FavoritesScreen(
    onHome: () -> Unit, modifier: Modifier = Modifier,
    products: List<Product> = emptyList(), onProduct: (String) -> Unit = {},
    onRemove: (Product) -> Unit = {}, busy: Boolean = false,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 640.dp).fillMaxWidth(), contentPadding = PaddingValues(FoodScanSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { FoodScanScreenTitle(stringResource(R.string.favorites_title)) }
            if (products.isEmpty()) item {
                FoodScanEmptyState(R.drawable.ic_favorite, stringResource(R.string.favorites_empty_title),
                    stringResource(R.string.favorites_empty_message)) {
                    FoodScanButton(stringResource(R.string.scan_product), onHome, Modifier.fillMaxWidth())
                }
            } else items(products, key = { it.id }) { product ->
                LibraryProductRow(product, product.category, { onProduct(product.id) }, { onRemove(product) },
                    stringResource(R.string.favorite_remove_item, product.name), busy)
            }
        }
    }
}

@Preview(name = "Favorites", widthDp = 360, locale = "ru")
@Preview(name = "Favorites • 200%", widthDp = 320, fontScale = 2f, locale = "ru")
@Composable
private fun FavoritesPreview() { FoodScanTheme { Surface { FavoritesScreen({}) } } }
