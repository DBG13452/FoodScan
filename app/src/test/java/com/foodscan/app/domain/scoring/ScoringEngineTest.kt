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
        nutrition = Nutrition(energyKcal = 150.0, proteinGrams = 12.0, fatGrams = 3.0,
            carbohydrateGrams = 10.0, sugarGrams = 5.0, saltGrams = .3,
            saturatedFatGrams = 1.5, fiberGrams = 6.0))

    private fun rated(p: Product = product, scenario: NutritionScenario = NutritionScenario.NORMAL) =
        ScoringEngine.evaluate(p, scenario) as ProductScore.Rated

    @Test fun approvedThresholdsProduceExpectedEndpointsAndMidpoint() {
        assertEquals(100, rated().value)
        val low = product.copy(nutrition = product.nutrition.copy(energyKcal = 400.0,
            sugarGrams = 22.5, carbohydrateGrams = 60.0, saturatedFatGrams = 5.0, fatGrams = 17.5,
            saltGrams = 1.5, proteinGrams = 0.0, fiberGrams = 0.0))
        assertEquals(0, rated(low).value)
        val mid = product.copy(nutrition = product.nutrition.copy(energyKcal = 275.0,
            sugarGrams = 13.75, carbohydrateGrams = 35.0, fatGrams = 10.25,
            saturatedFatGrams = 3.25, saltGrams = .9, proteinGrams = 6.0, fiberGrams = 3.0))
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

    @Test fun unknownsReduceCoverageAndZerosRemainKnownValues() {
        val missing = product.copy(nutrition = product.nutrition.copy(fiberGrams = null))
        val result = rated(missing)
        assertEquals(setOf(ScoreFactor.FIBER), result.missing)
        assertTrue(result.preliminary)
        assertEquals(90, result.coveredWeight)
        assertEquals(100, result.value)
        assertEquals(90, rated(product.copy(nutrition = product.nutrition.copy(fiberGrams = 0.0))).value)
        val carbsMissing = product.copy(nutrition = product.nutrition.copy(carbohydrateGrams = null))
        assertTrue(rated(carbsMissing, NutritionScenario.SUGAR_CONTROL).preliminary)
        assertTrue(rated(carbsMissing, NutritionScenario.NORMAL).preliminary)
    }

    private val ramen = product.copy(nutrition = Nutrition(energyKcal = 418.0, proteinGrams = 9.5,
        fatGrams = 12.0, carbohydrateGrams = 68.0))

    @Test fun ramenUsesKnownWeightsAndReportsTheFourMissingFactors() {
        val result = rated(ramen)
        assertEquals(29, result.value)
        assertEquals(40, result.coveredWeight)
        assertEquals(8, result.totalFactors)
        assertEquals(4, result.contributions.size)
        assertEquals(setOf(ScoreFactor.SUGAR, ScoreFactor.SATURATED_FAT, ScoreFactor.SALT, ScoreFactor.FIBER), result.missing)
        assertEquals(100.0, result.contributions.sumOf(result::normalizedWeight), .0001)
        assertEquals(29.2744253, result.contributions.sumOf(result::normalizedPoints), .0001)
        assertEquals("FoodScan v4", result.version)
        assertNull(ramen.nutrition.sugarGrams)
    }

    @Test fun additionalDataCanRaiseOrLowerThePreliminaryIndex() {
        val withSalt = rated(ramen.copy(nutrition = ramen.nutrition.copy(saltGrams = 2.0)))
        val withFiber = rated(ramen.copy(nutrition = ramen.nutrition.copy(fiberGrams = 6.0)))
        assertTrue(withSalt.value < rated(ramen).value)
        assertTrue(withFiber.value > rated(ramen).value)
        assertEquals(60, withSalt.coveredWeight)
        assertEquals(50, withFiber.coveredWeight)
        assertFalse(rated().preliminary)
    }

    @Test fun eachScenarioUsesItsOwnDenominatorForPartialData() {
        assertEquals(40, rated(ramen).coveredWeight)
        assertEquals(50, rated(ramen, NutritionScenario.SPORT).coveredWeight)
        assertEquals(45, rated(ramen, NutritionScenario.WEIGHT_CONTROL).coveredWeight)
        val sugar = rated(ramen, NutritionScenario.SUGAR_CONTROL)
        assertEquals(40, sugar.coveredWeight)
        assertEquals(8, sugar.totalFactors)
        assertEquals(25, sugar.value)
    }

    @Test fun aSingleRelevantValueAndInvalidPartialDataDoNotProduceScores() {
        listOf(Nutrition(energyKcal = 300.0), Nutrition(fatGrams = 10.0), Nutrition(carbohydrateGrams = 50.0),
            Nutrition(proteinGrams = 10.0), Nutrition()).forEach { n ->
            val result = ScoringEngine.evaluate(product.copy(nutrition = n), NutritionScenario.NORMAL) as ProductScore.Insufficient
            assertEquals(ScoreProblem.MISSING_DATA, result.problem)
        }
        val invalid = ScoringEngine.evaluate(ramen.copy(nutrition = ramen.nutrition.copy(saltGrams = Double.NaN)),
            NutritionScenario.NORMAL) as ProductScore.Insufficient
        assertEquals(ScoreProblem.INVALID_DATA, invalid.problem)
    }

    @Test fun moreSugarNeverRaisesTheScoreAndProteinAloneCannotGuaranteeIt() {
        val highSugar = product.copy(nutrition = product.nutrition.copy(sugarGrams = 8.0))
        assertTrue(rated(highSugar).value < rated().value)
        val onlyProtein = product.copy(nutrition = product.nutrition.copy(energyKcal = 400.0,
            sugarGrams = 30.0, carbohydrateGrams = 60.0, saturatedFatGrams = 5.0, fatGrams = 17.5,
            saltGrams = 2.0, fiberGrams = 0.0, proteinGrams = 20.0))
        assertEquals(25, rated(onlyProtein, NutritionScenario.SPORT).value)
    }

    @Test fun categorySelectsModelBeforePackageUnitAndUnknownTypeIsExplicit() {
        val drink = product.copy(category = "Juices", nutrition = product.nutrition.copy(
            basis = NutritionBasis.PER_100_G, energyKcal = 20.0, sugarGrams = 2.5,
            carbohydrateGrams = 2.5, fatGrams = 1.5, saltGrams = .3, saturatedFatGrams = .75))
        assertEquals(100, rated(drink).value)
        assertEquals(ScoreProfile.BEVERAGE, rated(drink).profile)
        assertFalse(rated(drink).assumedProfile)
        val unknown = product.copy(category = null, nutrition = product.nutrition.copy(basis = NutritionBasis.PER_100_G_OR_ML))
        assertTrue(rated(unknown).assumedProfile)
        assertTrue(rated(unknown).preliminary)
        assertEquals(100, rated(unknown).coveredWeight)
        assertEquals(NutritionBasis.PER_100_G_OR_ML, unknown.nutrition.basis)
        val tagged = drink.copy(category = null, categoryTags = listOf("en:beverages"))
        assertEquals(rated(drink).value, rated(tagged).value)
        val soup = product.copy(category = "Soups", nutrition = product.nutrition.copy(basis = NutritionBasis.PER_100_ML))
        assertEquals(ScoreProfile.FOOD, rated(soup).profile)
        assertEquals(rated().value, rated(soup).value)
    }

    @Test fun invalidNumbersContradictionsAndExcludedCategoriesAreRejected() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, -1.0).forEach { number ->
            assertTrue(ScoringEngine.evaluate(product.copy(nutrition = product.nutrition.copy(saltGrams = number)),
                NutritionScenario.NORMAL) is ProductScore.Insufficient)
        }
        assertTrue(ScoringEngine.evaluate(product.copy(nutrition = product.nutrition.copy(sugarGrams = 99.0)),
            NutritionScenario.NORMAL) is ProductScore.Insufficient)
        assertEquals(rated().value, rated(product.copy(category = null)).value)
        assertEquals(ScoreProblem.EXCLUDED_CATEGORY, (ScoringEngine.evaluate(product.copy(category = null, categoryTags = listOf("en:baby-foods")),
            NutritionScenario.NORMAL) as ProductScore.Insufficient).problem)
        assertTrue(ScoringEngine.evaluate(product.copy(category = "Non-alcoholic drinks"), NutritionScenario.NORMAL) is ProductScore.Rated)
    }

    @Test fun fatAndCarbohydratesAffectEveryScenarioAndAreNotOnlyDisplayed() {
        NutritionScenario.entries.forEach { scenario ->
            val baseline = rated(scenario = scenario)
            assertEquals(ScoreFactor.entries.toSet(), baseline.contributions.map { it.factor }.toSet())
            assertTrue(baseline.contributions.all { it.weight > 0 })
            val moreFat = rated(product.copy(nutrition = product.nutrition.copy(fatGrams = 17.5)), scenario)
            val moreCarbs = rated(product.copy(nutrition = product.nutrition.copy(carbohydrateGrams = 60.0)), scenario)
            assertTrue(moreFat.value < baseline.value)
            assertTrue(moreCarbs.value < baseline.value)
            assertEquals(4, rated(ramen, scenario).contributions.size)
        }
    }
}
