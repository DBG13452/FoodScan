package com.foodscan.app.data.product

import com.foodscan.app.domain.model.LookupError
import com.foodscan.app.domain.model.ProductBarcode
import com.foodscan.app.domain.model.ProductLookup
import com.foodscan.app.domain.model.ProductRepository
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class OpenFoodFactsRepository(
    private val api: OpenFoodFactsApi,
    // 12 requests/minute leaves headroom below OFF's current 15 product reads/min/IP.
    private val minRequestIntervalMs: Long = 5000,
    private val clock: () -> Long = System::currentTimeMillis,
) : ProductRepository {
    private val mutex = Mutex()
    private val cache = LinkedHashMap<String, Pair<Long, ProductLookup.Found>>()
    private var lastRequest = 0L
    private var retryAt = 0L
    suspend fun clearCache() = mutex.withLock { cache.clear() }

    override suspend fun findByBarcode(barcode: String): ProductLookup {
        val normalized = ProductBarcode.normalize(barcode)
            ?: return ProductLookup.Failed(LookupError.INVALID_BARCODE)
        val code = ProductBarcode.forLookup(normalized)
        val result: ProductLookup = mutex.withLock {
            cache[code]?.takeIf { clock() - it.first < 5 * 60_000 }?.let { return@withLock it.second }
            if (clock() < retryAt) return@withLock ProductLookup.Failed(
                LookupError.RATE_LIMIT, (retryAt - clock() + 999) / 1000,
            )
            delay((minRequestIntervalMs - (clock() - lastRequest)).coerceAtLeast(0))
            lastRequest = clock()
            try {
                val response = api.product(code)
                val result = when {
                    response.code() == 404 -> ProductLookup.Failed(LookupError.NOT_FOUND)
                    response.code() == 429 -> {
                        val header = response.headers()["Retry-After"]
                        val seconds = header?.toLongOrNull() ?: runCatching {
                            (ZonedDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME)
                                .toInstant().toEpochMilli() - clock()) / 1000
                        }.getOrNull() ?: 60L
                        val wait = seconds.coerceIn(1, 86_400)
                        retryAt = clock() + wait * 1000
                        ProductLookup.Failed(LookupError.RATE_LIMIT, wait)
                    }
                    response.code() >= 500 -> ProductLookup.Failed(LookupError.SERVER)
                    !response.isSuccessful -> ProductLookup.Failed(LookupError.SERVER)
                    response.body()?.product == null -> ProductLookup.Failed(LookupError.INVALID_RESPONSE)
                    response.body()?.status !in listOf("success", "success_with_warnings") ->
                        ProductLookup.Failed(LookupError.INVALID_RESPONSE)
                    else -> {
                        val dto: OffProductDto = response.body()!!.product!!
                        val found = ProductLookup.Found(dto.toProduct(code))
                        cache[code] = clock() to found
                        if (cache.size > 32) cache.remove(cache.keys.first())
                        found
                    }
                }
                response.errorBody()?.close()
                result
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: SocketTimeoutException) {
                ProductLookup.Failed(LookupError.TIMEOUT)
            } catch (_: JsonParseException) {
                ProductLookup.Failed(LookupError.INVALID_RESPONSE)
            } catch (_: MalformedJsonException) {
                ProductLookup.Failed(LookupError.INVALID_RESPONSE)
            } catch (_: IOException) {
                ProductLookup.Failed(LookupError.NETWORK)
            } catch (_: IllegalStateException) {
                ProductLookup.Failed(LookupError.INVALID_RESPONSE)
            }
        }
        // Manual input cannot identify the symbology of an ambiguous eight-digit code.
        val expanded = if (code.length == 8) ProductBarcode.fromCamera(code, isUpce = true) else null
        if (result is ProductLookup.Failed && result.reason == LookupError.NOT_FOUND && expanded != null && expanded != code) {
            val fallback = findByBarcode(expanded)
            if (fallback is ProductLookup.Found) mutex.withLock {
                cache[code] = clock() to fallback
                if (cache.size > 32) cache.remove(cache.keys.first())
            }
            return fallback
        }
        return result
    }
}

object ProductServices {
    private val instance by lazy { OpenFoodFactsRepository(OpenFoodFactsApi.create()) }
    val repository: ProductRepository get() = instance
    suspend fun clearCache() = instance.clearCache()
}
