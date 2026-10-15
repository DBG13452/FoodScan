package com.foodscan.app.data.product

import com.foodscan.app.domain.model.Nutrition
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductSource

/** Fictional fixtures for UI development, not facts about any real brand or barcode. */
object DemoProductRepository {
    val products: List<Product> = listOf(
        Product(
            id = "demo-oatmeal",
            name = "Овсяные хлопья",
            category = "Крупы и злаки",
            manufacturer = "Учебный пример",
            nutrition = Nutrition(
                energyKcal = 370.0, proteinGrams = 13.0, fatGrams = 7.0,
                carbohydrateGrams = 60.0, sugarGrams = 1.0, saltGrams = 0.0,
                saturatedFatGrams = 1.2, fiberGrams = 10.0,
            ),
            ingredients = "Хлопья овсяные цельнозерновые.",
            allergens = listOf("Овёс (глютен)"),
            traces = null,
            additives = emptyList(),
            source = ProductSource.DEMO,
        ),
        Product(
            id = "demo-yogurt",
            name = "Натуральный йогурт",
            category = "Молочные продукты",
            nutrition = Nutrition(
                energyKcal = 65.0, proteinGrams = 4.0, fatGrams = 3.2,
                carbohydrateGrams = 5.0, sugarGrams = 5.0,
            ),
            ingredients = "Молоко, закваска.",
            allergens = listOf("Молоко"),
            traces = emptyList(),
            source = ProductSource.DEMO,
        ),
        Product(
            id = "demo-bread",
            name = "Цельнозерновой хлеб",
            category = "Хлеб и выпечка",
            source = ProductSource.DEMO,
        ),
    )

    fun findById(id: String): Product? = products.firstOrNull { it.id == id }
}
