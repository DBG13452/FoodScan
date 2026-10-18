package com.foodscan.app.domain.scoring

import com.foodscan.app.domain.model.NutritionScenario
import com.foodscan.app.domain.model.Product
import kotlin.math.roundToInt

enum class ScoreFactor { SUGAR, SATURATED_FAT, SALT, FIBER, PROTEIN, ENERGY, CARBOHYDRATES, FAT }

data class FactorContribution(
    val factor: ScoreFactor,
    val value: Double,
    val lower: Double,
    val upper: Double,
    val weight: Int,
    val quality: Double,
) { val points: Double get() = quality * weight }

sealed interface ProductScore {
    data class Rated(
        val value: Int,
        val scenario: NutritionScenario,
        val contributions: List<FactorContribution>,
        val missing: Set<ScoreFactor>,
        val version: String = ScoringEngine.VERSION,
        val profile: ScoreProfile = ScoreProfile.FOOD,
        val assumedProfile: Boolean = false,
    ) : ProductScore {
        val preliminary: Boolean get() = missing.isNotEmpty() || assumedProfile
        /** Coverage of the formula's weights, not a probability of correctness. */
        val coveredWeight: Int get() = contributions.sumOf { it.weight }
        val totalFactors: Int get() = contributions.size + missing.size
        fun normalizedPoints(contribution: FactorContribution): Double = contribution.points * 100 / coveredWeight
        fun normalizedWeight(contribution: FactorContribution): Double = contribution.weight * 100.0 / coveredWeight
    }
    data class Insufficient(val missing: Set<ScoreFactor>, val problem: ScoreProblem) : ProductScore
}

enum class ScoreProblem { MISSING_DATA, UNKNOWN_BASIS, UNKNOWN_CATEGORY, EXCLUDED_CATEGORY, INVALID_DATA }

/** User-approved educational formula. Not Nutri-Score or a medical/safety assessment. */
object ScoringEngine {
    const val VERSION = "FoodScan v4"
    fun evaluate(product: Product, scenario: NutritionScenario): ProductScore {
        val n = product.nutrition
        val categories = product.category.orEmpty().lowercase(java.util.Locale.ROOT).replace('-', ' ')
        val excludedTags = setOf("en:baby-foods", "en:baby-milks", "en:dietary-supplements",
            "en:alcoholic-beverages", "en:foods-for-special-medical-purposes")
        val excluded = listOf("infant", "baby food", "supplement", "medical food", "детск", "бад", "лечебн")
        val alcoholPercent = n.alcoholPercent
        if (alcoholPercent != null && (!alcoholPercent.isFinite() || alcoholPercent !in 0.0..100.0))
            return ProductScore.Insufficient(emptySet(), ScoreProblem.INVALID_DATA)
        val alcoholFree = Regex("non[ -]?alcohol|alcohol[ -]?free|безалкоголь").containsMatchIn(categories) ||
            product.categoryTags.any { it in setOf("en:non-alcoholic-beverages", "en:non-alcoholic-beers", "en:alcohol-free-beers") }
        val culinaryCategory = Regex("vinegar|уксус|sauce|соус|confectionery|конфет").containsMatchIn(categories)
        val alcoholCategory = !culinaryCategory && Regex("(?<![\\p{L}\\p{N}])(?:alcohol[\\p{L}]*|beers?|wines?|spirits|liqueurs?|ciders?|" +
            "алкоголь[\\p{L}]*|пиво|пива|вин[оа]|водк[\\p{L}]*|коньяк[\\p{L}]*|виски|ликер[\\p{L}]*|сидр[\\p{L}]*)(?![\\p{L}\\p{N}])")
            .containsMatchIn(categories.replace('ё', 'е'))
        val alcohol = (alcoholPercent != null && alcoholPercent > 0) || (!alcoholFree &&
            (alcoholCategory || product.categoryTags.any { it in setOf("en:beers", "en:wines", "en:spirits", "en:liqueurs", "en:ciders") }))
        if (excluded.any(categories::contains) || alcohol || product.categoryTags.any { it in excludedTags })
            return ProductScore.Insufficient(emptySet(), ScoreProblem.EXCLUDED_CATEGORY)
        val selection = ScoreProfileResolver.resolve(product)
        val beverage = selection.profile == ScoreProfile.BEVERAGE
        val weights = if (beverage) when (scenario) {
            NutritionScenario.NORMAL, NutritionScenario.SPORT -> listOf(40, 5, 10, 0, 0, 25, 15, 5)
            NutritionScenario.WEIGHT_CONTROL -> listOf(35, 5, 5, 0, 0, 35, 15, 5)
            NutritionScenario.SUGAR_CONTROL -> listOf(50, 5, 5, 0, 0, 20, 15, 5)
        } else when (scenario) {
            NutritionScenario.NORMAL -> listOf(15, 15, 20, 10, 10, 10, 10, 10)
            NutritionScenario.SPORT -> listOf(10, 15, 15, 10, 25, 10, 5, 10)
            NutritionScenario.WEIGHT_CONTROL -> listOf(15, 10, 15, 15, 15, 15, 5, 10)
            NutritionScenario.SUGAR_CONTROL -> listOf(25, 10, 15, 10, 10, 10, 15, 5)
        }
        val values = listOf(n.sugarGrams, n.saturatedFatGrams, n.saltGrams, n.fiberGrams,
            n.proteinGrams, n.energyKcal, n.carbohydrateGrams, n.fatGrams)
        val missing = ScoreFactor.entries.filterIndexed { index, _ -> weights[index] > 0 && values[index] == null }.toSet()
        val invalid = values.filterNotNull().any { !it.isFinite() || it < 0 } ||
            (n.sugarGrams != null && n.carbohydrateGrams != null && n.sugarGrams > n.carbohydrateGrams) ||
            (n.saturatedFatGrams != null && n.fatGrams != null && n.saturatedFatGrams > n.fatGrams)
        if (invalid) return ProductScore.Insufficient(emptySet(), ScoreProblem.INVALID_DATA)
        val knownCount = weights.indices.count { weights[it] > 0 && values[it] != null }
        // Fat/salt alone must not yield a reassuring score for an otherwise unknown drink.
        if (knownCount < 2 || (beverage && n.sugarGrams == null && n.energyKcal == null))
            return ProductScore.Insufficient(missing, ScoreProblem.MISSING_DATA)
        val thresholds = if (beverage) listOf(
            2.5 to 11.25, .75 to 2.5, .3 to .75, 0.0 to 3.0, 0.0 to 6.0, 20.0 to 80.0, 2.5 to 12.0, 1.5 to 8.75,
        ) else listOf(5.0 to 22.5, 1.5 to 5.0, .3 to 1.5, 0.0 to 6.0, 0.0 to 12.0, 150.0 to 400.0, 10.0 to 60.0, 3.0 to 17.5)
        val contributions = ScoreFactor.entries.mapIndexedNotNull { index, factor ->
            if (weights[index] == 0 || values[index] == null) null else {
                val (low, high) = thresholds[index]
                val value = requireNotNull(values[index])
                val increasing = factor == ScoreFactor.FIBER || factor == ScoreFactor.PROTEIN
                val fraction = ((value - low) / (high - low)).coerceIn(0.0, 1.0)
                FactorContribution(factor, value, low, high, weights[index], if (increasing) fraction else 1 - fraction)
            }
        }
        val knownWeight = contributions.sumOf { it.weight }
        val rawPoints = contributions.sumOf { it.points }
        return ProductScore.Rated((if (knownWeight == 100) rawPoints else rawPoints * 100 / knownWeight)
            .roundToInt().coerceIn(0, 100), scenario, contributions, missing,
            profile = selection.profile, assumedProfile = selection.assumed)
    }
}
