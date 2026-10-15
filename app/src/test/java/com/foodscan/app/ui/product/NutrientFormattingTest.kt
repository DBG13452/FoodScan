package com.foodscan.app.ui.product

import org.junit.Assert.assertEquals
import org.junit.Test

class NutrientFormattingTest {
    @Test
    fun unknownAndInvalidValuesAreNotShownAsZero() {
        listOf(null, Double.NaN, Double.POSITIVE_INFINITY, -1.0).forEach { value ->
            assertEquals("Нет данных", formatNutrient(value, "г", "Нет данных"))
        }
    }

    @Test
    fun explicitZeroAndSmallNonzeroValuesRemainDistinct() {
        assertEquals("0 г", formatNutrient(0.0, "г", "Нет данных"))
        assertEquals("0,0001 г", formatNutrient(0.0001, "г", "Нет данных"))
        assertEquals("3,2 г", formatNutrient(3.2, "г", "Нет данных"))
        assertEquals("370 ккал", formatNutrient(370.0, "ккал", "Нет данных"))
    }
}
