package com.foodscan.app.domain.model

sealed interface ProductLookup {
    data class Found(val product: Product) : ProductLookup
    data class Failed(val reason: LookupError, val retryAfterSeconds: Long? = null) : ProductLookup
}

enum class LookupError { INVALID_BARCODE, NOT_FOUND, NETWORK, TIMEOUT, RATE_LIMIT, SERVER, INVALID_RESPONSE }

fun interface ProductRepository {
    suspend fun findByBarcode(barcode: String): ProductLookup
}
