package com.foodscan.app.ui.favorites

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.foodscan.app.R
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanMessage
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanSectionTitle

@Composable
fun FavoritesScreen(onHome: () -> Unit, modifier: Modifier = Modifier) {
    FoodScanScreenColumn(modifier) {
        FoodScanSectionTitle(stringResource(R.string.favorites_title))
        FoodScanMessage(
            title = stringResource(R.string.favorites_empty_title),
            message = stringResource(R.string.favorites_empty_message),
            modifier = Modifier.fillMaxWidth(),
        )
        FoodScanButton(stringResource(R.string.back_home), onHome, Modifier.fillMaxWidth())
    }
}
