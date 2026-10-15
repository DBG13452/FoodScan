package com.foodscan.app.data.local

import com.foodscan.app.domain.model.Nutrition
import com.foodscan.app.domain.model.NutritionBasis
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductSource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/** Explicit keys avoid reflection over domain classes and remain stable with R8. */
object ProductSnapshot {
    fun encode(product: Product): String = JsonObject().apply {
        addProperty("version", 1)
        addProperty("id", product.id); addProperty("name", product.name)
        addProperty("category", product.category); addProperty("manufacturer", product.manufacturer)
        addProperty("barcode", product.barcode); addProperty("source", product.source.name)
        addProperty("brands", product.brands); addProperty("image", product.imageUrl)
        addProperty("ingredients", product.ingredients)
        fun list(key: String, value: List<String>?) {
            if (value != null) add(key, JsonArray().also { array -> value.forEach(array::add) })
        }
        list("allergens", product.allergens); list("traces", product.traces); list("additives", product.additives)
        list("categoryTags", product.categoryTags)
        addProperty("nutriScore", product.nutriScore); addProperty("nova", product.novaGroup)
        add("nutrition", JsonObject().apply {
            val n = product.nutrition
            addProperty("basis", n.basis.name); addProperty("energy", n.energyKcal)
            addProperty("protein", n.proteinGrams); addProperty("fat", n.fatGrams)
            addProperty("carbs", n.carbohydrateGrams); addProperty("sugar", n.sugarGrams)
            addProperty("salt", n.saltGrams); addProperty("saturated", n.saturatedFatGrams)
            addProperty("fiber", n.fiberGrams)
        })
    }.toString()

    fun decode(payload: String): Product? = runCatching {
        val json = JsonParser.parseString(payload).asJsonObject
        require(json.get("version").asInt == 1)
        fun text(key: String) = json.get(key)?.takeUnless { it.isJsonNull }?.asString
        fun list(key: String) = json.getAsJsonArray(key)?.map { it.asString }
        val n = json.getAsJsonObject("nutrition")
        fun number(key: String) = n.get(key)?.takeUnless { it.isJsonNull }?.asDouble
        Product(
            id = requireNotNull(text("id")), name = requireNotNull(text("name")), category = text("category"),
            manufacturer = text("manufacturer"), barcode = text("barcode"),
            source = ProductSource.valueOf(requireNotNull(text("source"))), brands = text("brands"),
            ingredients = text("ingredients"), imageUrl = text("image"),
            allergens = list("allergens"), traces = list("traces"), additives = list("additives"),
            nutriScore = text("nutriScore"), novaGroup = text("nova")?.toIntOrNull(),
            categoryTags = list("categoryTags").orEmpty(),
            nutrition = Nutrition(
                basis = NutritionBasis.valueOf(n.get("basis").asString), energyKcal = number("energy"),
                proteinGrams = number("protein"), fatGrams = number("fat"), carbohydrateGrams = number("carbs"),
                sugarGrams = number("sugar"), saltGrams = number("salt"), saturatedFatGrams = number("saturated"),
                fiberGrams = number("fiber"),
            ),
        )
    }.getOrNull()
}
