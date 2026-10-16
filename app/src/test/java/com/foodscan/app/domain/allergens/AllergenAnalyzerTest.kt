package com.foodscan.app.domain.allergens

import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductSource
import org.junit.Assert.*
import org.junit.Test

class AllergenAnalyzerTest {
    private fun product(text: String?, api: List<String>? = null, traces: List<String>? = null) =
        Product("test", "Test", "Food", ingredients = text, allergens = api, traces = traces, source = ProductSource.OPEN_FOOD_FACTS)
    private fun analyze(text: String) = AllergenAnalyzer.analyze(product(text))

    @Test fun ramenUnitesApiAndIngredientsAndKeepsTheWholeTraceList() {
        val p = product("Лапша: пшеничная мука, клейковина, соевый соус. " +
            "Продукт содержит глютен, продукты переработки молока, сои и следы продуктов переработки рыбы, ракообразных, Моллюсков.", listOf("Молоко"))
        val before = p.copy()
        val result = AllergenAnalyzer.analyze(p)
        assertEquals(setOf("Глютен", "Молоко", "Соя"), result.contains.map { it.name }.toSet())
        assertEquals(setOf("Рыба", "Ракообразные", "Моллюски"), result.traces.map { it.name }.toSet())
        assertEquals(setOf(AllergenOrigin.API, AllergenOrigin.INGREDIENTS), result.contains.first { it.name == "Молоко" }.origins)
        assertTrue(result.traces.all { it.origins == setOf(AllergenOrigin.INGREDIENTS) })
        assertEquals(before, p)
    }

    @Test fun negationAndFreeClaimsDoNotBecomeContainsClaims() {
        val result = analyze("Без глютена. Не содержит молока, сои и яиц. Не содержит следов рыбы и моллюсков. Gluten-free, milk-free.")
        assertTrue(result.contains.isEmpty())
        assertTrue(result.traces.isEmpty())
    }

    @Test fun lactoseFreeMilkIsStillMilkAndAcidsAreNotMilk() {
        assertEquals(listOf("Молоко"), analyze("Молоко без лактозы.").contains.map { it.name })
        assertEquals(listOf("Молоко"), analyze("Безлактозное молоко.").contains.map { it.name })
        assertTrue(analyze("Молоко без лактозы.").conflicts.isEmpty())
        assertTrue(analyze("Молочная кислота, лактат натрия, молочнокислые бактерии.").contains.isEmpty())
    }

    @Test fun traceScopeEndsAtANewSentenceOrContainsMarker() {
        val result = analyze("Может содержать молоко и орехи. Состав: пшеничная мука, соевый соус. Следы рыбы, содержит яйца.")
        assertEquals(setOf("Глютен", "Соя", "Яйца"), result.contains.map { it.name }.toSet())
        assertEquals(setOf("Молоко", "Орехи", "Рыба"), result.traces.map { it.name }.toSet())
    }

    @Test fun sourceConflictsAreNotResolvedBySilentlyDroppingApiEvidence() {
        val result = AllergenAnalyzer.analyze(product("Без молока. Содержит сою.", listOf("en:milk"), listOf("Рыба")))
        assertEquals(setOf("Молоко", "Соя"), result.contains.map { it.name }.toSet())
        assertEquals(listOf("Молоко"), result.conflicts)
        assertEquals(listOf("Рыба"), result.traces.map { it.name })
    }

    @Test fun ingredientSynonymsAndWordBoundariesAvoidCommonFalseMatches() {
        val result = analyze("Кокосовое молоко, кокосовый орех, мускатный орех, сироп глюкозы. " +
            "Ароматизатор со вкусом рыбы. Арахис, кунжут.")
        assertEquals(setOf("Арахис", "Кунжут"), result.contains.map { it.name }.toSet())
        assertTrue(analyze("Растительные сливки. Сырный ароматизатор.").contains.isEmpty())
    }

    @Test fun blankUnknownAndRepeatedSourcesDoNotMeanAllergenFree() {
        assertTrue(AllergenAnalyzer.analyze(product(null)).contains.isEmpty())
        val result = AllergenAnalyzer.analyze(product("Молоко, молочная сыворотка.", listOf("Молоко", "unknown:tag"), listOf("Молоко")))
        assertEquals(setOf("Молоко", "unknown:tag"), result.contains.map { it.name }.toSet())
        assertTrue(result.traces.isEmpty())
    }

    @Test fun commaAfterWithoutDoesNotHideAnUnrelatedIngredient() {
        assertEquals(listOf("Молоко"), analyze("Без глютена, сухое молоко.").contains.map { it.name })
        assertEquals(listOf("Соя"), analyze("Без молока и глютена, соевая мука.").contains.map { it.name })
    }
}
