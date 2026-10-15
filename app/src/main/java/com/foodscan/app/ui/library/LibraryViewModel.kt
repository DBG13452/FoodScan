package com.foodscan.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.foodscan.app.data.AppContainer
import com.foodscan.app.data.local.ViewedProduct
import com.foodscan.app.domain.model.NutritionScenario
import com.foodscan.app.domain.model.Product
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryState(
    val favorites: List<Product> = emptyList(), val history: List<ViewedProduct> = emptyList(),
    val scenario: NutritionScenario = NutritionScenario.NORMAL, val historyEnabled: Boolean = true,
    val loaded: Boolean = false,
)

class LibraryViewModel(private val services: AppContainer) : ViewModel() {
    val error = MutableStateFlow(false)
    val busy = MutableStateFlow(false)
    val state = combine(services.library.favorites, services.library.history,
        services.settings.scenario, services.settings.historyEnabled) { favorites, history, scenario, enabled ->
        LibraryState(favorites, history, scenario, enabled, loaded = true)
    }.catch { error.value = true; emit(LibraryState(loaded = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryState())
    fun dismissError() { error.value = false }
    private fun write(block: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { block() } catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) { error.value = true }
            finally { busy.value = false }
        }
    }
    fun removeFavorite(product: Product) = write { services.library.setFavorite(product, false) }
    fun deleteHistory(id: Long) = write { services.library.deleteHistory(id) }
    fun clearHistory() = write { services.library.clearHistory() }
    fun selectScenario(scenario: NutritionScenario) = write { services.settings.setScenario(scenario) }
    fun setHistoryEnabled(enabled: Boolean) = write { services.settings.setHistoryEnabled(enabled) }
    fun clearAll() = write { services.clearUserData() }
    companion object {
        fun factory(services: AppContainer) = viewModelFactory { initializer { LibraryViewModel(services) } }
    }
}
