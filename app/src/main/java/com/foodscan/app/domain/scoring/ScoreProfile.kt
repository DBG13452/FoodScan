package com.foodscan.app.domain.scoring

import com.foodscan.app.domain.model.NutritionBasis
import com.foodscan.app.domain.model.Product
import java.util.Locale

enum class ScoreProfile { FOOD, BEVERAGE }

internal data class ProfileSelection(val profile: ScoreProfile, val assumed: Boolean = false)

/** Categories describe use; package volume alone does not make soup or oil a drink. */
internal object ScoreProfileResolver {
    private val foodExceptions = setOf("en:soups", "en:sauces", "en:condiments", "en:oils",
        "en:vegetable-oils", "en:syrups", "en:milk-powders", "en:powdered-milks", "en:beverage-powders")
    private val drinks = setOf("en:beverages", "en:fruit-juices", "en:vegetable-juices", "en:fruit-nectars",
        "en:waters", "en:milks", "en:plant-based-milks", "en:plant-based-beverages", "en:sodas",
        "en:drinkable-yogurts", "en:drinking-yogurts", "en:fermented-milk-drinks", "en:kefirs",
        "en:energy-drinks", "en:tea-based-beverages", "en:coffee-based-beverages")
    private val foods = setOf("en:yogurts", "en:cheeses", "en:cereals", "en:cereal-flakes", "en:noodles",
        "en:instant-noodles", "en:biscuits", "en:chocolates", "en:breads", "en:snacks", "en:fruits",
        "en:vegetables", "en:meats", "en:fishes", "en:ice-creams", "en:purees")
    private fun words(pattern: String) = Regex("(?<![\\p{L}\\p{N}])(?:$pattern)(?![\\p{L}\\p{N}])")
    private val foodExceptionWords = words("soups?|sauces?|syrups?|oils?|milk powders?|суп[\\p{L}]*|соус[\\p{L}]*|масл[\\p{L}]*|сироп[\\p{L}]*|сухое молоко|сухие напитки")
    private val drinkWords = words("beverages?|drinks?|juices?|nectars?|waters?|milks?|smoothies?|kefirs?|" +
        "сок[\\p{L}]*|нектар[\\p{L}]*|напит[\\p{L}]*|вод[аы]|молоко|кефир[\\p{L}]*|ряженк[\\p{L}]*|айран[\\p{L}]*|смузи|питьев[\\p{L}]* йогурт[\\p{L}]*")
    private val foodWords = words("yogurts?|yoghurts?|cheeses?|cereals?|flakes?|noodles?|biscuits?|chocolates?|breads?|" +
        "йогурт[\\p{L}]*|сыр[\\p{L}]*|хлопья|лапша|печень[\\p{L}]*|шоколад[\\p{L}]*|хлеб[\\p{L}]*|каша|каши")

    fun resolve(product: Product): ProfileSelection {
        val tags = product.categoryTags.map { it.lowercase(Locale.ROOT) }.toSet()
        val category = product.category.orEmpty().lowercase(Locale.ROOT).replace('ё', 'е').replace('-', ' ')
        if (tags.any(foodExceptions::contains) || foodExceptionWords.containsMatchIn(category))
            return ProfileSelection(ScoreProfile.FOOD)
        if (tags.any(drinks::contains) || drinkWords.containsMatchIn(category))
            return ProfileSelection(ScoreProfile.BEVERAGE)
        if (tags.any(foods::contains) || foodWords.containsMatchIn(category))
            return ProfileSelection(ScoreProfile.FOOD)
        return ProfileSelection(if (product.nutrition.basis == NutritionBasis.PER_100_ML)
            ScoreProfile.BEVERAGE else ScoreProfile.FOOD, assumed = true)
    }
}
