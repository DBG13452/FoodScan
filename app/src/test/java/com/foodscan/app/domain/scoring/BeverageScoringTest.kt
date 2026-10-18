package com.foodscan.app.domain.scoring

import com.foodscan.app.domain.model.Nutrition
import com.foodscan.app.domain.model.NutritionBasis
import com.foodscan.app.domain.model.NutritionScenario
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductSource
import org.junit.Assert.*
import org.junit.Test

class BeverageScoringTest {
    private val juice = Product("juice", "Сок", "Juices", source = ProductSource.OPEN_FOOD_FACTS,
        categoryTags = listOf("en:beverages", "en:fruit-juices"),
        nutrition = Nutrition(basis = NutritionBasis.PER_100_ML, energyKcal = 45.0,
            proteinGrams = 0.0, fatGrams = 0.0, carbohydrateGrams = 10.0, sugarGrams = 10.0,
            saturatedFatGrams = 0.0, saltGrams = 0.0, fiberGrams = 0.0))
    private fun score(p: Product = juice, scenario: NutritionScenario = NutritionScenario.NORMAL) =
        ScoringEngine.evaluate(p, scenario) as ProductScore.Rated

    @Test fun juiceHasItsOwnReproducibleModelAndSixFactors() {
        val result = score()
        assertEquals(43, result.value)
        assertEquals(ScoreProfile.BEVERAGE, result.profile)
        assertEquals(6, result.totalFactors)
        assertEquals(100, result.coveredWeight)
        assertFalse(result.preliminary)
        assertEquals(100.0, result.contributions.sumOf(result::normalizedWeight), .0001)
        assertEquals(43.4555138, result.contributions.sumOf(result::normalizedPoints), .0001)
        val food = juice.copy(category = "Cereals", categoryTags = emptyList())
        assertEquals(76, score(food).value)
        assertEquals(ScoreProfile.FOOD, score(food).profile)
    }

    @Test fun proteinAndFiberCannotPenalizeOrRewardDrinksInAnyScenario() {
        NutritionScenario.entries.forEach { scenario ->
            val zero = score(scenario = scenario)
            val unknown = score(juice.copy(nutrition = juice.nutrition.copy(proteinGrams = null, fiberGrams = null)), scenario)
            val high = score(juice.copy(nutrition = juice.nutrition.copy(proteinGrams = 10.0, fiberGrams = 5.0)), scenario)
            assertEquals(zero, unknown)
            assertEquals(zero, high)
            assertFalse(ScoreFactor.PROTEIN in zero.missing)
            assertFalse(ScoreFactor.FIBER in zero.missing)
        }
        assertEquals(score().value, score(scenario = NutritionScenario.SPORT).value)
    }

    @Test fun unknownActiveFactorsRemainUnknownAndReduceOnlyCoverage() {
        val partial = score(juice.copy(nutrition = juice.nutrition.copy(saltGrams = null)))
        assertTrue(partial.preliminary)
        assertEquals(setOf(ScoreFactor.SALT), partial.missing)
        assertEquals(90, partial.coveredWeight)
        assertEquals(6, partial.totalFactors)
        assertEquals(100.0, partial.contributions.sumOf(partial::normalizedWeight), .0001)
    }

    @Test fun aDrinkNeedsTwoActiveFactorsIncludingSugarOrEnergy() {
        listOf(Nutrition(saltGrams = 0.0, fatGrams = 0.0, saturatedFatGrams = 0.0),
            Nutrition(energyKcal = 45.0, proteinGrams = 1.0, fiberGrams = 1.0)).forEach { n ->
            val result = ScoringEngine.evaluate(juice.copy(nutrition = n), NutritionScenario.NORMAL) as ProductScore.Insufficient
            assertEquals(ScoreProblem.MISSING_DATA, result.problem)
            assertFalse(ScoreFactor.PROTEIN in result.missing)
        }
        assertTrue(score(juice.copy(nutrition = Nutrition(energyKcal = 45.0, sugarGrams = 10.0))).preliminary)
    }

    @Test fun moreSugarEnergyOrFatCannotRaiseTheBeverageScore() {
        NutritionScenario.entries.forEach { scenario ->
            val baseline = score(scenario = scenario)
            listOf(juice.nutrition.copy(sugarGrams = 10.5), juice.nutrition.copy(energyKcal = 70.0),
                juice.nutrition.copy(fatGrams = 10.0)).forEach { nutrition ->
                val adjusted = nutrition.copy(carbohydrateGrams = 11.0)
                assertTrue(score(juice.copy(nutrition = adjusted), scenario).value < baseline.value)
            }
            assertEquals(100, baseline.contributions.sumOf { it.weight })
        }
    }

    @Test fun zeroCalorieUnsweetenedWaterDoesNotNeedProteinOrFiber() {
        val water = juice.copy(category = "Waters", nutrition = juice.nutrition.copy(
            energyKcal = 0.0, carbohydrateGrams = 0.0, sugarGrams = 0.0, proteinGrams = null, fiberGrams = null))
        NutritionScenario.entries.forEach { assertEquals(100, score(water, it).value) }
    }

    @Test fun milkAndDrinkingYogurtAreDrinksButSpoonableYogurtIsFood() {
        listOf("Milks", "Plant-based milks", "Питьевой йогурт", "Кефир").forEach { category ->
            assertEquals(ScoreProfile.BEVERAGE, score(juice.copy(category = category, categoryTags = emptyList())).profile)
        }
        assertEquals(ScoreProfile.FOOD, score(juice.copy(category = "Yogurts", categoryTags = listOf("en:yogurts"))).profile)
        assertEquals(ScoreProfile.BEVERAGE, score(juice.copy(category = "Yogurts", categoryTags = listOf("en:yogurts", "en:drinkable-yogurts"))).profile)
    }

    @Test fun saucesSoupsOilsAndPowdersAreNotDrinksDespiteMlOrBroadBeverageTags() {
        listOf("Soups", "Соусы", "Vegetable oils", "Milk powders", "Сиропы").forEach { category ->
            assertEquals(ScoreProfile.FOOD, score(juice.copy(category = category, categoryTags = emptyList())).profile)
        }
        assertEquals(ScoreProfile.FOOD, score(juice.copy(category = null,
            categoryTags = listOf("en:beverages", "en:beverage-powders"))).profile)
    }

    @Test fun tagsOverrideUnitAndUnrecognizedTypeIsMarkedAsAssumed() {
        val inGrams = juice.copy(nutrition = juice.nutrition.copy(basis = NutritionBasis.PER_100_G))
        assertEquals(score().value, score(inGrams).value)
        val unknown = juice.copy(category = null, categoryTags = emptyList())
        assertTrue(score(unknown).assumedProfile)
        assertTrue(score(unknown).preliminary)
        val noUnit = juice.copy(nutrition = juice.nutrition.copy(basis = NutritionBasis.PER_100_G_OR_ML))
        assertFalse(score(noUnit).assumedProfile)
        assertEquals(score().value, score(noUnit).value)
    }

    @Test fun alcoholIsExcludedByStrengthEvenWithoutCategoryAndBySpecificTags() {
        listOf(juice.copy(category = null, categoryTags = emptyList(), nutrition = juice.nutrition.copy(alcoholPercent = 5.0)),
            juice.copy(category = null, categoryTags = listOf("en:beers")),
            juice.copy(category = "Вино", categoryTags = emptyList()),
            juice.copy(category = null, categoryTags = listOf("en:alcoholic-beverages"))).forEach {
            assertEquals(ScoreProblem.EXCLUDED_CATEGORY,
                (ScoringEngine.evaluate(it, NutritionScenario.NORMAL) as ProductScore.Insufficient).problem)
        }
    }

    @Test fun nonAlcoholicBeerAndWineVinegarDoNotMatchAlcoholByAccident() {
        assertTrue(ScoringEngine.evaluate(juice.copy(category = "Non-alcoholic beers", categoryTags = listOf("en:beers", "en:non-alcoholic-beers")),
            NutritionScenario.NORMAL) is ProductScore.Rated)
        assertTrue(ScoringEngine.evaluate(juice.copy(category = "Wine vinegar", categoryTags = emptyList()),
            NutritionScenario.NORMAL) is ProductScore.Rated)
        assertEquals(ScoreProblem.EXCLUDED_CATEGORY, (ScoringEngine.evaluate(juice.copy(category = "Безалкогольное пиво",
            nutrition = juice.nutrition.copy(alcoholPercent = .5)), NutritionScenario.NORMAL) as ProductScore.Insufficient).problem)
    }
}
