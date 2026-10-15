package com.foodscan.app.domain.scoring

import com.foodscan.app.domain.model.NutritionBasis
import com.foodscan.app.domain.model.NutritionScenario
import com.foodscan.app.domain.model.Product
import kotlin.math.roundToInt

enum class ScoreFactor { SUGAR, SATURATED_FAT, SALT, FIBER, PROTEIN, ENERGY, CARBOHYDRATES }

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
        val version: String = "FoodScan v1",
    ) : ProductScore
    data class Insufficient(val missing: Set<ScoreFactor>, val problem: ScoreProblem) : ProductScore
}

enum class ScoreProblem { MISSING_DATA, UNKNOWN_BASIS, UNKNOWN_CATEGORY, EXCLUDED_CATEGORY, INVALID_DATA }

/** User-approved educational formula. Not Nutri-Score or a medical/safety assessment. */
object ScoringEngine {
    fun evaluate(product: Product, scenario: NutritionScenario): ProductScore {
        val n = product.nutrition
        if (product.category.isNullOrBlank()) return ProductScore.Insufficient(emptySet(), ScoreProblem.UNKNOWN_CATEGORY)
        val categories = product.category.lowercase().replace('-', ' ')
        val excludedTags = setOf("en:baby-foods", "en:baby-milks", "en:dietary-supplements",
            "en:alcoholic-beverages", "en:foods-for-special-medical-purposes")
        val excluded = listOf("infant", "baby food", "supplement", "medical food", "детск", "бад", "лечебн")
        val alcohol = ("alcohol" in categories && "non alcohol" !in categories && "alcohol free" !in categories) ||
            ("алкоголь" in categories && "безалкоголь" !in categories)
        if (excluded.any(categories::contains) || alcohol || product.categoryTags.any { it in excludedTags })
            return ProductScore.Insufficient(emptySet(), ScoreProblem.EXCLUDED_CATEGORY)
        if (n.basis == NutritionBasis.PER_100_G_OR_ML) return ProductScore.Insufficient(emptySet(), ScoreProblem.UNKNOWN_BASIS)
        val weights = when (scenario) {
            NutritionScenario.NORMAL -> listOf(20, 20, 20, 15, 10, 15, 0)
            NutritionScenario.SPORT -> listOf(15, 20, 15, 10, 25, 15, 0)
            NutritionScenario.WEIGHT_CONTROL -> listOf(20, 15, 15, 15, 15, 20, 0)
            NutritionScenario.SUGAR_CONTROL -> listOf(30, 15, 15, 10, 10, 10, 10)
        }
        val values = listOf(n.sugarGrams, n.saturatedFatGrams, n.saltGrams, n.fiberGrams,
            n.proteinGrams, n.energyKcal, n.carbohydrateGrams)
        val missing = ScoreFactor.entries.filterIndexed { index, _ -> weights[index] > 0 && values[index] == null }.toSet()
        if (missing.isNotEmpty()) return ProductScore.Insufficient(missing, ScoreProblem.MISSING_DATA)
        val invalid = values.filterNotNull().any { !it.isFinite() || it < 0 } ||
            (n.fatGrams != null && (!n.fatGrams.isFinite() || n.fatGrams < 0)) ||
            (n.sugarGrams != null && n.carbohydrateGrams != null && n.sugarGrams > n.carbohydrateGrams) ||
            (n.saturatedFatGrams != null && n.fatGrams != null && n.saturatedFatGrams > n.fatGrams)
        if (invalid) return ProductScore.Insufficient(emptySet(), ScoreProblem.INVALID_DATA)
        val thresholds = if (n.basis == NutritionBasis.PER_100_ML) listOf(
            2.5 to 11.25, .75 to 2.5, .3 to .75, 0.0 to 3.0, 0.0 to 6.0, 20.0 to 80.0, 2.5 to 12.0,
        ) else listOf(5.0 to 22.5, 1.5 to 5.0, .3 to 1.5, 0.0 to 6.0, 0.0 to 12.0, 150.0 to 400.0, 10.0 to 60.0)
        val contributions = ScoreFactor.entries.mapIndexedNotNull { index, factor ->
            if (weights[index] == 0) null else {
                val (low, high) = thresholds[index]
                val value = requireNotNull(values[index])
                val increasing = factor == ScoreFactor.FIBER || factor == ScoreFactor.PROTEIN
                val fraction = ((value - low) / (high - low)).coerceIn(0.0, 1.0)
                FactorContribution(factor, value, low, high, weights[index], if (increasing) fraction else 1 - fraction)
            }
        }
        return ProductScore.Rated(contributions.sumOf { it.points }.roundToInt().coerceIn(0, 100), scenario, contributions)
    }
}
