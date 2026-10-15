package com.foodscan.app.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodscan.app.data.AppContainer
import com.foodscan.app.data.product.DemoProductRepository
import com.foodscan.app.data.product.ProductServices
import com.foodscan.app.domain.model.LookupError
import com.foodscan.app.domain.model.NutritionScenario
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductLookup
import com.foodscan.app.domain.model.ProductRepository
import com.foodscan.app.navigation.ProductDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface ProductUiState {
    data object Loading : ProductUiState
    data class Ready(val product: Product) : ProductUiState
    data class Error(val failure: ProductLookup.Failed) : ProductUiState
}

class ProductViewModel(
    private val savedState: SavedStateHandle,
    private val services: AppContainer,
    private val repository: ProductRepository = ProductServices.repository,
) : ViewModel() {
    private val productId: String = savedState[ProductDestination.argument] ?: ""
    private val mutableState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val state = mutableState.asStateFlow()
    val scenario = services.settings.scenario.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NutritionScenario.NORMAL)
    val favorite = services.library.isFavorite(productId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val storageError = MutableStateFlow(false)
    val favoriteBusy = MutableStateFlow(false)
    private var request: Job? = null

    init { load() }

    fun load() {
        if (request?.isActive == true) return
        request = viewModelScope.launch {
            mutableState.value = ProductUiState.Loading
            val saved = try { services.library.find(productId) }
                catch (cancel: CancellationException) { throw cancel }
                catch (_: Exception) { storageError.value = true; null }
            val local = saved ?: DemoProductRepository.findById(productId)
            val result = when {
                local != null -> ProductLookup.Found(local)
                productId.any { it !in '0'..'9' } -> ProductLookup.Failed(LookupError.NOT_FOUND)
                else -> repository.findByBarcode(productId)
            }
            mutableState.value = when (result) {
                is ProductLookup.Found -> {
                    try {
                        val record = savedState.get<Boolean>("historyRecorded") != true && services.settings.historyEnabled.first()
                        services.library.recordView(result.product, record)
                        savedState["historyRecorded"] = true
                    } catch (cancel: CancellationException) { throw cancel }
                    catch (_: Exception) { storageError.value = true }
                    ProductUiState.Ready(result.product)
                }
                is ProductLookup.Failed -> ProductUiState.Error(result)
            }
        }
    }

    fun toggleFavorite() {
        val product = (mutableState.value as? ProductUiState.Ready)?.product ?: return
        if (favoriteBusy.value) return
        favoriteBusy.value = true
        viewModelScope.launch {
            try { services.library.setFavorite(product, !services.library.isFavorite(product.id).first()) }
            catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) { storageError.value = true }
            finally { favoriteBusy.value = false }
        }
    }

    companion object {
        fun factory(services: AppContainer) = viewModelFactory {
            initializer { ProductViewModel(createSavedStateHandle(), services) }
        }
    }
}
