package com.foodscan.app.data.product

import com.foodscan.app.domain.model.LookupError
import com.foodscan.app.domain.model.NutritionBasis
import com.foodscan.app.domain.model.ProductLookup
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class OpenFoodFactsRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: OpenFoodFactsRepository
    @Before fun setup() {
        server = MockWebServer().apply { start() }
        repository = OpenFoodFactsRepository(OpenFoodFactsApi.create(server.url("/").toString()), minRequestIntervalMs = 0)
    }
    @After fun teardown() { server.shutdown() }

    @Test fun sendsIdentifiedMinimalRequestAndCachesSuccess() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"status":"success","product":{"code":"3017620422003","product_name":"Example","nutriments":{"salt_100g":0}}}"""))
        val first = repository.findByBarcode("3017620422003") as ProductLookup.Found
        assertEquals(0.0, first.product.nutrition.saltGrams!!, 0.0)
        assertNull(first.product.nutrition.sugarGrams)
        assertEquals(first, repository.findByBarcode("3017620422003"))
        assertEquals(1, server.requestCount)
        val request = server.takeRequest()
        assertEquals(OpenFoodFactsApi.USER_AGENT, request.getHeader("User-Agent"))
        assertEquals("/api/v3/product/3017620422003", request.requestUrl!!.encodedPath)
        assertEquals(OpenFoodFactsApi.FIELDS, request.requestUrl!!.queryParameter("fields"))
    }

    @Test fun mapsNotFoundServerAndMalformedResponses() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404))
        assertEquals(LookupError.NOT_FOUND, (repository.findByBarcode("3017620422003") as ProductLookup.Failed).reason)
        server.enqueue(MockResponse().setResponseCode(503))
        assertEquals(LookupError.SERVER, (repository.findByBarcode("3017620422003") as ProductLookup.Failed).reason)
        server.enqueue(MockResponse().setBody("<html>bad response</html>"))
        assertEquals(LookupError.INVALID_RESPONSE, (repository.findByBarcode("3017620422003") as ProductLookup.Failed).reason)
        server.enqueue(MockResponse().setBody("""{"status":"success"}"""))
        assertEquals(LookupError.INVALID_RESPONSE, (repository.findByBarcode("3017620422003") as ProductLookup.Failed).reason)
    }

    @Test fun respectsRetryAfterWithoutAnotherRequest() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "120"))
        val first = repository.findByBarcode("3017620422003") as ProductLookup.Failed
        assertEquals(LookupError.RATE_LIMIT, first.reason)
        assertEquals(120L, first.retryAfterSeconds)
        assertEquals(LookupError.RATE_LIMIT, (repository.findByBarcode("96385074") as ProductLookup.Failed).reason)
        assertEquals(1, server.requestCount)
    }

    @Test fun rejectsInvalidCodesBeforeNetworking() = runBlocking {
        assertEquals(LookupError.INVALID_BARCODE, (repository.findByBarcode("123") as ProductLookup.Failed).reason)
        assertEquals(0, server.requestCount)
    }

    @Test fun ambiguousUpceRetriesExpandedCodeOnlyAfterNotFound() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404))
        server.enqueue(MockResponse().setBody("""{"status":"success","product":{"code":"010000000009","product_name":"Example"}}"""))
        val result = repository.findByBarcode("01000009") as ProductLookup.Found
        assertEquals("010000000009", result.product.barcode)
        assertEquals("/api/v3/product/01000009", server.takeRequest().requestUrl!!.encodedPath)
        assertEquals("/api/v3/product/010000000009", server.takeRequest().requestUrl!!.encodedPath)
        assertEquals(result, repository.findByBarcode("01000009"))
        assertEquals(2, server.requestCount)
    }

    @Test fun mappingUsesOnlyNormalizedValuesAndNeverAssumesAllergensAbsent() {
        val dto = Gson().fromJson("""{
            "product_name":"Original","product_name_ru":"Название","brands":"Brand",
            "categories":"Food, Spreads, fr:custom-tag",
            "product_quantity_unit":"ml", "allergens_tags":[], "additives_tags":[],
            "nutriments":{"fat_serving":9,"fat_value":9,"salt_100g":0,"sugars_100g":"unknown","fiber_100g":-1},
            "ingredients_text":"Original ingredients", "ingredients_text_ru":"Состав",
            "nutriscore_grade":"unknown","nova_group":7
        }""", OffProductDto::class.java).toProduct("96385074")
        assertEquals("Название", dto.name)
        assertEquals("Spreads", dto.category)
        assertEquals("Состав", dto.ingredients)
        assertEquals("Brand", dto.brands)
        assertNull(dto.manufacturer)
        assertNull(dto.nutrition.fatGrams)
        assertNull(dto.nutrition.sugarGrams)
        assertNull(dto.nutrition.fiberGrams)
        assertEquals(NutritionBasis.PER_100_ML, dto.nutrition.basis)
        assertNull(dto.allergens)
        assertNull(dto.additives)
        assertNull(dto.nutriScore)
        assertNull(dto.novaGroup)
    }

    @Test fun networkErrorsAreRecoverableAndCancellationIsPreserved() = runBlocking {
        suspend fun lookupWith(error: Exception): ProductLookup = OpenFoodFactsRepository(object : OpenFoodFactsApi {
            override suspend fun product(barcode: String, fields: String, language: String, type: String): retrofit2.Response<OffResponseDto> {
                throw error
            }
        }, 0).findByBarcode("96385074")
        assertEquals(LookupError.NETWORK, (lookupWith(IOException()) as ProductLookup.Failed).reason)
        assertEquals(LookupError.TIMEOUT, (lookupWith(SocketTimeoutException()) as ProductLookup.Failed).reason)
        try {
            lookupWith(CancellationException())
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }

    @Test fun ramenMappingKeepsMissingTotalsSeparateFromEstimatedAddedSugar() {
        val mapped = Gson().fromJson("""{
            "product_name":"Cheese рамен", "categories":"Instant noodles", "product_quantity_unit":"g",
            "allergens_tags":["en:milk"], "traces_tags":[],
            "ingredients_text_ru":"Содержит глютен, молоко, сою и следы рыбы, ракообразных, моллюсков.",
            "nutriments":{"energy-kcal_100g":418,"proteins_100g":9.5,"fat_100g":12,"carbohydrates_100g":68,
                "added-sugars_100g":0.4827,"added-sugars_modifier":"~"}
        }""", OffProductDto::class.java).toProduct("4607065580728")
        assertNull(mapped.nutrition.sugarGrams)
        assertNull(mapped.nutrition.saltGrams)
        assertEquals(29, (com.foodscan.app.domain.scoring.ScoringEngine.evaluate(mapped,
            com.foodscan.app.domain.model.NutritionScenario.NORMAL) as com.foodscan.app.domain.scoring.ProductScore.Rated).value)
        val summary = com.foodscan.app.domain.allergens.AllergenAnalyzer.analyze(mapped)
        assertEquals(setOf("Глютен", "Молоко", "Соя"), summary.contains.map { it.name }.toSet())
        assertEquals(setOf("Рыба", "Ракообразные", "Моллюски"), summary.traces.map { it.name }.toSet())
    }

    @Test fun reportedYogurtWithoutPackageQuantityStillUsesAllFiveMeasurements() {
        val mapped = Gson().fromJson("""{
            "code":"4690228106903", "product_name":"Йогурт Чудо", "categories":"Yogurts",
            "nutrition_data_per":"100g", "categories_tags":["en:dairies","en:yogurts"],
            "nutriments":{"energy-kcal_100g":84,"proteins_100g":2.8,"fat_100g":1.9,
                "carbohydrates_100g":13.9,"sugars_100g":1}
        }""", OffProductDto::class.java).toProduct("4690228106903")
        assertEquals(NutritionBasis.PER_100_G_OR_ML, mapped.nutrition.basis)
        val score = com.foodscan.app.domain.scoring.ScoringEngine.evaluate(mapped,
            com.foodscan.app.domain.model.NutritionScenario.NORMAL) as com.foodscan.app.domain.scoring.ProductScore.Rated
        assertEquals(85, score.value)
        assertEquals(5, score.contributions.size)
        assertEquals(55, score.coveredWeight)
        assertTrue(score.preliminary)
        assertTrue(score.generalScale)
    }

    @Test fun normalizedEnergyAndSodiumAreConvertedOnlyWhenDirectValuesAreMissing() {
        fun map(nutrients: String) = Gson().fromJson("""{"nutriments":$nutrients}""",
            OffProductDto::class.java).toProduct("test").nutrition
        val converted = map("""{"energy-kj_100g":418.4,"sodium_100g":0.4}""")
        assertEquals(100.0, converted.energyKcal!!, .0001)
        assertEquals(1.0, converted.saltGrams!!, .0001)
        assertEquals(100.0, map("""{"energy_100g":418.4}""").energyKcal!!, .0001)
        val direct = map("""{"energy-kcal_100g":0,"energy-kj_100g":418.4,"salt_100g":0,"sodium_100g":0.4}""")
        assertEquals(0.0, direct.energyKcal!!, 0.0)
        assertEquals(0.0, direct.saltGrams!!, 0.0)
        val inexact = map("""{"energy-kj_100g":418.4,"energy-kj_modifier":"~",
            "sodium_100g":0.4,"sodium_modifier":"<","energy_serving":100}""")
        assertNull(inexact.energyKcal)
        assertNull(inexact.saltGrams)
    }
}
