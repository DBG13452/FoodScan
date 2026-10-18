package com.foodscan.app.data.product

import com.foodscan.app.domain.model.Nutrition
import com.foodscan.app.domain.model.NutritionBasis
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductSource
import java.util.Locale

internal fun OffProductDto.toProduct(requestedBarcode: String): Product {
    fun String?.clean() = this?.trim()?.takeIf { it.isNotEmpty() }
    fun nutrient(key: String): Double? {
        val modifier = nutriments?.get("${key}_modifier")?.takeIf { it.isJsonPrimitive }?.asString
        // The current UI represents exact amounts only, not bounds such as '< 0.1 g'.
        if (!modifier.isNullOrBlank() && modifier != "=") return null
        return nutriments?.get("${key}_100g")?.let { element ->
            if (!element.isJsonPrimitive) null else element.asString.toDoubleOrNull()
        }?.takeIf { it.isFinite() && it >= 0 }
    }

    // OFF normalized energy is kJ and sodium is grams, irrespective of input units.
    // Prefer the reported kcal / salt; equivalent measurements are fallbacks, not
    // additional factors (which would count the same information twice).
    val energyKcal = nutrient("energy-kcal") ?: (nutrient("energy-kj") ?: nutrient("energy"))?.div(4.184)
    val saltGrams = nutrient("salt") ?: nutrient("sodium")?.times(2.5)

    // OFF _100g values may mean 100 ml. Do not guess the basis from a product name.
    val declaredUnit = quantityUnit?.lowercase(Locale.ROOT) ?: quantity?.let {
        Regex("(?:\\d[\\d.,]*\\s*)(kg|mg|ml|cl|dl|g|l|кг|мг|мл|г|л)\\s*$", RegexOption.IGNORE_CASE)
            .find(it)?.groupValues?.get(1)?.lowercase(Locale.ROOT)
    }
    val basis = when (declaredUnit) {
        "ml", "l", "cl", "dl", "мл", "л" -> NutritionBasis.PER_100_ML
        "g", "kg", "mg", "г", "кг", "мг" -> NutritionBasis.PER_100_G
        else -> NutritionBasis.PER_100_G_OR_ML
    }
    val nova = novaGroup?.takeIf { it.isJsonPrimitive }?.asString?.toIntOrNull()?.takeIf { it in 1..4 }
    return Product(
        id = requestedBarcode,
        name = russianName.clean() ?: name.clean() ?: "Продукт $requestedBarcode",
        // OFF returns the whole category hierarchy; the card needs one readable category.
        category = categories.clean()?.split(',')?.map(String::trim)
            ?.lastOrNull { it.isNotEmpty() && ':' !in it },
        // brands does not identify the legal manufacturer.
        brands = brands.clean(),
        barcode = code.clean() ?: requestedBarcode,
        nutrition = Nutrition(
            basis = basis,
            energyKcal = energyKcal,
            proteinGrams = nutrient("proteins"), fatGrams = nutrient("fat"),
            carbohydrateGrams = nutrient("carbohydrates"), sugarGrams = nutrient("sugars"),
            saltGrams = saltGrams, saturatedFatGrams = nutrient("saturated-fat"),
            fiberGrams = nutrient("fiber"),
            alcoholPercent = nutrient("alcohol")?.takeIf { it <= 100 },
        ),
        ingredients = russianIngredients.clean() ?: ingredients.clean(),
        // An empty API tag array is not proof that allergens or additives are absent.
        allergens = allergens?.map(::allergenLabel)?.filter(String::isNotBlank)?.takeIf { it.isNotEmpty() },
        traces = traces?.map(::allergenLabel)?.filter(String::isNotBlank)?.takeIf { it.isNotEmpty() },
        additives = additives?.map { it.removePrefix("en:").uppercase(Locale.ROOT) }
            ?.filter(String::isNotBlank)?.takeIf { it.isNotEmpty() },
        imageUrl = imageUrl?.takeIf { it.startsWith("https://images.openfoodfacts.org/") },
        nutriScore = nutriScore?.lowercase(Locale.ROOT)?.takeIf { it in listOf("a", "b", "c", "d", "e") }
            ?.uppercase(Locale.ROOT),
        novaGroup = nova,
        source = ProductSource.OPEN_FOOD_FACTS,
        categoryTags = categoryTags.orEmpty(),
    )
}

private fun allergenLabel(tag: String): String = when (tag) {
    "en:milk" -> "Молоко"
    "en:eggs" -> "Яйца"
    "en:gluten" -> "Глютен"
    "en:soybeans" -> "Соя"
    "en:nuts" -> "Орехи"
    "en:peanuts" -> "Арахис"
    "en:fish" -> "Рыба"
    "en:crustaceans" -> "Ракообразные"
    "en:molluscs" -> "Моллюски"
    "en:celery" -> "Сельдерей"
    "en:mustard" -> "Горчица"
    "en:sesame-seeds" -> "Кунжут"
    "en:lupin" -> "Люпин"
    "en:sulphur-dioxide-and-sulphites" -> "Диоксид серы и сульфиты"
    else -> tag.removePrefix("ru:") // Keep unknown language tags rather than inventing a translation.
}
