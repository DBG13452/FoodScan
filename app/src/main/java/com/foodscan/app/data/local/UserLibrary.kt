package com.foodscan.app.data.local

import com.foodscan.app.domain.model.Product
import kotlinx.coroutines.flow.map

data class ViewedProduct(val eventId: Long, val product: Product, val viewedAt: Long)

class UserLibrary(private val dao: LibraryDao) {
    val favorites = dao.favorites().map { rows -> rows.mapNotNull { ProductSnapshot.decode(it.payload) } }
    val history = dao.history().map { rows -> rows.mapNotNull { row ->
        ProductSnapshot.decode(row.payload)?.let { ViewedProduct(row.eventId, it, row.viewedAt) }
    } }
    fun isFavorite(id: String) = dao.isFavorite(id)
    suspend fun find(id: String) = dao.product(id)?.let { ProductSnapshot.decode(it.payload) }
    suspend fun recordView(product: Product, recordHistory: Boolean) = dao.recordView(
        StoredProduct(product.id, ProductSnapshot.encode(product)), System.currentTimeMillis(), recordHistory,
    )
    suspend fun setFavorite(product: Product, selected: Boolean) = dao.setFavorite(
        StoredProduct(product.id, ProductSnapshot.encode(product)), selected, System.currentTimeMillis(),
    )
    suspend fun deleteHistory(id: Long) = dao.deleteHistory(id)
    suspend fun clearHistory() = dao.clearHistory()
    suspend fun clearAll() = dao.clearAll()
}
