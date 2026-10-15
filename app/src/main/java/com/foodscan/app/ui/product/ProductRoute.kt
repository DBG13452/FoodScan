package com.foodscan.app.ui.product

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foodscan.app.R
import com.foodscan.app.data.AppContainer
import com.foodscan.app.domain.model.LookupError
import com.foodscan.app.ui.components.FoodScanBackButton
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanMessage
import com.foodscan.app.ui.components.FoodScanMessageTone
import com.foodscan.app.ui.components.FoodScanScreenColumn

@Composable
fun ProductRoute(
    onBack: () -> Unit, services: AppContainer, onAdditive: (String) -> Unit,
    onMethodology: () -> Unit,
    model: ProductViewModel = viewModel(factory = ProductViewModel.factory(services)),
) {
    val state by model.state.collectAsStateWithLifecycle()
    val scenario by model.scenario.collectAsStateWithLifecycle()
    val favorite by model.favorite.collectAsStateWithLifecycle()
    val favoriteBusy by model.favoriteBusy.collectAsStateWithLifecycle()
    val storageError by model.storageError.collectAsStateWithLifecycle()
    when (val current = state) {
        is ProductUiState.Ready -> {
            if (storageError) androidx.compose.material3.AlertDialog(
                onDismissRequest = { model.storageError.value = false },
                title = { Text(stringResource(R.string.storage_error)) },
                text = { Text(stringResource(R.string.storage_error_message)) },
                confirmButton = { FoodScanButton(stringResource(R.string.action_close), { model.storageError.value = false }) },
            )
            ProductScreen(current.product, onBack, scenario = scenario, isFavorite = favorite,
                favoriteBusy = favoriteBusy, onFavorite = model::toggleFavorite,
                onAdditive = onAdditive, onMethodology = onMethodology)
        }
        ProductUiState.Loading -> FoodScanScreenColumn {
            FoodScanBackButton(onBack)
            CircularProgressIndicator()
            Text(stringResource(R.string.product_loading))
        }
        is ProductUiState.Error -> FoodScanScreenColumn {
            FoodScanBackButton(onBack)
            val reason = current.failure.reason
            FoodScanMessage(
                title = stringResource(if (reason == LookupError.NOT_FOUND) R.string.product_not_found else R.string.product_load_failed),
                message = stringResource(when (reason) {
                    LookupError.NOT_FOUND -> R.string.product_off_not_found
                    LookupError.INVALID_BARCODE -> R.string.scanner_invalid_barcode
                    LookupError.NETWORK -> R.string.product_network_error
                    LookupError.TIMEOUT -> R.string.product_timeout
                    LookupError.RATE_LIMIT -> R.string.product_rate_limit
                    LookupError.SERVER -> R.string.product_server_error
                    LookupError.INVALID_RESPONSE -> R.string.product_response_error
                }),
                modifier = Modifier.fillMaxWidth(), tone = FoodScanMessageTone.Error,
            )
            current.failure.retryAfterSeconds?.let { Text(stringResource(R.string.product_retry_after, it)) }
            if (reason !in listOf(LookupError.NOT_FOUND, LookupError.INVALID_BARCODE)) {
                FoodScanButton(stringResource(R.string.product_retry), model::load, Modifier.fillMaxWidth())
            }
        }
    }
}
