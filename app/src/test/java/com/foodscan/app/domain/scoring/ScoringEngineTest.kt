package com.foodscan.app.domain.scoring

import com.foodscan.app.domain.model.Nutrition
import com.foodscan.app.domain.model.NutritionBasis
import com.foodscan.app.domain.model.NutritionScenario
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductSource
import org.junit.Assert.*
import org.junit.Test

class ScoringEngineTest {
    private val product = Product("test", "Test", "Cereals", source = ProductSource.DEMO,
        nutrition = Nutrition(energyKcal = 150.0, proteinGrams = 12.0, fatGrams = 4.0,
            carbohydrateGrams = 20.0, sugarGrams = 5.0, saltGrams = .3,
            saturatedFatGrams = 1.5, fiberGrams = 6.0))

    private fun rated(p: Product = product, scenario: NutritionScenario = NutritionScenario.NORMAL) =
        ScoringEngine.evaluate(p, scenario) as ProductScore.Rated

    @Test fun approvedThresholdsProduceExpectedEndpointsAndMidpoint() {
        assertEquals(100, rated().value)
        val low = product.copy(nutrition = product.nutrition.copy(energyKcal = 400.0,
            sugarGrams = 22.5, carbohydrateGrams = 60.0, saturatedFatGrams = 5.0, fatGrams = 10.0,
            saltGrams = 1.5, proteinGrams = 0.0, fiberGrams = 0.0))
        assertEquals(0, rated(low).value)
        val mid = product.copy(nutrition = product.nutrition.copy(energyKcal = 275.0,
            sugarGrams = 13.75, saturatedFatGrams = 3.25, saltGrams = .9, proteinGrams = 6.0, fiberGrams = 3.0))
        assertEquals(50, rated(mid).value)
    }

    @Test fun scenarioReweightsWithoutChangingTheSource() {
        val before = product.copy(nutrition = product.nutrition.copy(proteinGrams = 0.0))
        val original = before.copy()
        assertEquals(90, rated(before).value)
        assertEquals(75, rated(before, NutritionScenario.SPORT).value)
        assertEquals(85, rated(before, NutritionScenario.WEIGHT_CONTROL).value)
        assertEquals(original, before)
        NutritionScenario.entries.forEach { scenario ->
            val result = rated(scenario = scenario)
            assertEquals(100, result.contributions.sumOf { it.weight })
            assertTrue(result.value in 0..100)
            assertEquals(result, rated(scenario = scenario))
        }
    }

    @Test fun requiredUnknownsAreNotIgnoredButExplicitZerosCount() {
        val missing = product.copy(nutrition = product.nutrition.copy(fiberGrams = null))
        val result = ScoringEngine.evaluate(missing, NutritionScenario.NORMAL) as ProductScore.Insufficient
        assertEquals(setOf(ScoreFactor.FIBER), result.missing)
        assertEquals(85, rated(product.copy(nutrition = product.nutrition.copy(fiberGrams = 0.0))).value)
        val carbsMissing = product.copy(nutrition = product.nutrition.copy(carbohydrateGrams = null))
        assertTrue(ScoringEngine.evaluate(carbsMissing, NutritionScenario.SUGAR_CONTROL) is ProductScore.Insufficient)
        assertTrue(ScoringEngine.evaluate(carbsMissing, NutritionScenario.NORMAL) is ProductScore.Rated)
    }

    @Test fun moreSugarNeverRaisesTheScoreAndProteinAloneCannotGuaranteeIt() {
        val highSugar = product.copy(nutrition = product.nutrition.copy(sugarGrams = 20.0))
        assertTrue(rated(highSugar).value < rated().value)
        val onlyProtein = product.copy(nutrition = product.nutrition.copy(energyKcal = 400.0,
            sugarGrams = 30.0, carbohydrateGrams = 60.0, saturatedFatGrams = 5.0, fatGrams = 10.0,
            saltGrams = 2.0, fiberGrams = 0.0, proteinGrams = 20.0))
        assertEquals(25, rated(onlyProtein, NutritionScenario.SPORT).value)
    }

    @Test fun liquidsUseDifferentThresholdsAndUnknownBasisIsNotGuessed() {
        val liquid = product.copy(nutrition = product.nutrition.copy(basis = NutritionBasis.PER_100_ML,
            energyKcal = 20.0, sugarGrams = 2.5, saltGrams = .3, saturatedFatGrams = .75, proteinGrams = 6.0, fiberGrams = 3.0))
        assertEquals(100, rated(liquid).value)
        assertEquals(ScoreProblem.UNKNOWN_BASIS, (ScoringEngine.evaluate(product.copy(
            nutrition = product.nutrition.copy(basis = NutritionBasis.PER_100_G_OR_ML)), NutritionScenario.NORMAL) as ProductScore.Insufficient).problem)
    }

    @Test fun invalidNumbersContradictionsAndExcludedCategoriesAreRejected() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, -1.0).forEach { number ->
            assertTrue(ScoringEngine.evaluate(product.copy(nutrition = product.nutrition.copy(saltGrams = number)),
                NutritionScenario.NORMAL) is ProductScore.Insufficient)
        }
        assertTrue(ScoringEngine.evaluate(product.copy(nutrition = product.nutrition.copy(sugarGrams = 99.0)),
            NutritionScenario.NORMAL) is ProductScore.Insufficient)
        assertEquals(ScoreProblem.UNKNOWN_CATEGORY, (ScoringEngine.evaluate(product.copy(category = null),
            NutritionScenario.NORMAL) as ProductScore.Insufficient).problem)
        assertEquals(ScoreProblem.EXCLUDED_CATEGORY, (ScoringEngine.evaluate(product.copy(categoryTags = listOf("en:baby-foods")),
            NutritionScenario.NORMAL) as ProductScore.Insufficient).problem)
        assertTrue(ScoringEngine.evaluate(product.copy(category = "Non-alcoholic drinks"), NutritionScenario.NORMAL) is ProductScore.Rated)
    }
}
