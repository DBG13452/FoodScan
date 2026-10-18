package com.foodscan.app.data.local

import com.foodscan.app.domain.model.Nutrition
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductSource
import org.junit.Assert.*
import org.junit.Test

class ProductSnapshotTest {
    @Test fun preservesNullZeroEmptyAndSourceValues() {
        val product = Product("id", "Название", "Категория", source = ProductSource.OPEN_FOOD_FACTS,
            allergens = null, additives = emptyList(), nutrition = Nutrition(saltGrams = 0.0),
            categoryTags = listOf("en:cereals"), ingredients = "Состав с \"кавычками\"")
        assertEquals(product, ProductSnapshot.decode(ProductSnapshot.encode(product)))
    }
    @Test fun corruptOrUnknownSnapshotDoesNotCrash() {
        assertNull(ProductSnapshot.decode("not json"))
        assertNull(ProductSnapshot.decode("{\"version\":77}"))
    }
    @Test fun alcoholIsPreservedAndOldSnapshotsRemainReadable() {
        val product = Product("beer", "Beer", null, source = ProductSource.OPEN_FOOD_FACTS,
            nutrition = Nutrition(alcoholPercent = 5.0))
        val encoded = ProductSnapshot.encode(product)
        assertEquals(product, ProductSnapshot.decode(encoded))
        val old = com.google.gson.JsonParser.parseString(encoded).asJsonObject
        old.getAsJsonObject("nutrition").remove("alcohol")
        assertEquals(product.copy(nutrition = product.nutrition.copy(alcoholPercent = null)),
            ProductSnapshot.decode(old.toString()))
    }
}
