package com.foodscan.app.product

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foodscan.app.TestEnvironment
import com.foodscan.app.domain.model.Nutrition
import com.foodscan.app.domain.model.NutritionBasis
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.model.ProductSource
import com.foodscan.app.ui.FoodScanApp
import com.foodscan.app.ui.product.ProductScreen
import com.foodscan.app.ui.theme.FoodScanTheme
import com.foodscan.app.waitForText
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Deterministic sample of the reported OFF fields, never inserted into the user's actual library. */
@RunWith(AndroidJUnit4::class)
class PartialProductTest {
    @get:Rule(order = 0) val environment = TestEnvironment()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<ComponentActivity>()
    private val ramen = Product("4607065580728", "Cheese рамен", "Instant noodles", source = ProductSource.OPEN_FOOD_FACTS,
        nutrition = Nutrition(energyKcal = 418.0, proteinGrams = 9.5, fatGrams = 12.0, carbohydrateGrams = 68.0),
        ingredients = "Лапша: пшеничная мука, клейковина, соевый соус. Продукт содержит глютен, " +
            "продукты переработки молока, сои и следы продуктов переработки рыбы, ракообразных, Моллюсков.",
        allergens = listOf("Молоко"))

    @Before fun keepAwake() {
        compose.runOnUiThread { compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    @Test fun storedSnapshotGetsPartialScoreAndIngredientAllergensWithoutRefetching() {
        runBlocking { environment.services.library.recordView(ramen, true) }
        compose.setContent { FoodScanTheme { FoodScanApp(services = environment.services) } }
        compose.waitForText("Cheese рамен")
        compose.onNodeWithText("Cheese рамен").performScrollTo().performClick()
        compose.waitForText("Предварительная оценка")
        compose.onNodeWithText("29 / 100").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Учтено показателей: 4 из 8").performScrollTo().assertIsDisplayed()
        screenshot("partial-score")
        compose.onNodeWithText("Почему такая оценка").performScrollTo().performClick()
        compose.onNodeWithText("Известные показатели покрывают 40%", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Жиры: 12 г").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Углеводы: 68 г").performScrollTo().assertIsDisplayed()
        screenshot("score-factors")
        compose.onNodeWithText("Молоко, Глютен, Соя").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Возможны следы: Рыба, Ракообразные, Моллюски").performScrollTo().assertIsDisplayed()
        screenshot("allergens")
    }

    @Test fun preliminaryScoreAndAllergenSourcesFitNarrowScreenAtLargeFont() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                FoodScanTheme { Box(Modifier.width(320.dp)) { ProductScreen(ramen, {}) } }
            }
        }
        compose.onNodeWithText("Предварительная оценка").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Учтено показателей: 4 из 8").performScrollTo().assertIsDisplayed()
        screenshot("partial-large-font")
        compose.onNodeWithText("Возможны следы: Рыба, Ракообразные, Моллюски").performScrollTo().assertIsDisplayed()
        screenshot("allergens-large-font")
    }

    @Test fun savedYogurtWithUnknownBasisDisplaysScoreAndAllKnownFactors() {
        val yogurt = Product("4690228106903", "Йогурт Чудо", "Yogurts", source = ProductSource.OPEN_FOOD_FACTS,
            nutrition = Nutrition(basis = NutritionBasis.PER_100_G_OR_ML, energyKcal = 84.0,
                proteinGrams = 2.8, fatGrams = 1.9, carbohydrateGrams = 13.9, sugarGrams = 1.0))
        runBlocking { environment.services.library.recordView(yogurt, true) }
        compose.setContent { FoodScanTheme { FoodScanApp(services = environment.services) } }
        compose.waitForText("Йогурт Чудо")
        compose.onNodeWithText("Йогурт Чудо").performScrollTo().performClick()
        compose.waitForText("Предварительная оценка")
        compose.onNodeWithText("85 / 100").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Учтено показателей: 5 из 8").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Модель: Еда").performScrollTo().assertIsDisplayed()
        screenshot("yogurt-score")
        compose.onNodeWithText("Почему такая оценка").performScrollTo().performClick()
        compose.onNodeWithText("Известные показатели покрывают 55% веса методики").performScrollTo().assertIsDisplayed()
        listOf("Сахар: 1 г", "Белки: 2,8 г", "Калорийность: 84 ккал", "Углеводы: 13,9 г", "Жиры: 1,9 г").forEach {
            compose.onNodeWithText(it).performScrollTo().assertIsDisplayed()
        }
    }

    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "foodscan-v4-screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
