package com.foodscan.app.domain.model

/** Source values only. A missing measurement must never be replaced with zero. */
data class Product(
    val id: String,
    val name: String,
    val category: String?,
    val manufacturer: String? = null,
    val barcode: String? = null,
    val nutrition: Nutrition = Nutrition(),
    val ingredients: String? = null,
    // null = no information; empty = explicitly not declared in the source.
    val allergens: List<String>? = null,
    val traces: List<String>? = null,
    val additives: List<String>? = null,
    val source: ProductSource,
    val brands: String? = null,
    val imageUrl: String? = null,
    val nutriScore: String? = null,
    val novaGroup: Int? = null,
    val categoryTags: List<String> = emptyList(),
)

enum class ProductSource { DEMO, OPEN_FOOD_FACTS }

enum class NutritionBasis { PER_100_G, PER_100_ML, PER_100_G_OR_ML }

data class Nutrition(
    val basis: NutritionBasis = NutritionBasis.PER_100_G,
    val energyKcal: Double? = null,
    val proteinGrams: Double? = null,
    val fatGrams: Double? = null,
    val carbohydrateGrams: Double? = null,
    val sugarGrams: Double? = null,
    val saltGrams: Double? = null,
    val saturatedFatGrams: Double? = null,
    val fiberGrams: Double? = null,
    val alcoholPercent: Double? = null,
)
