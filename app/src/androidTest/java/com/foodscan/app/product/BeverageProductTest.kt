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
import com.foodscan.app.ui.product.ProductScreen
import com.foodscan.app.ui.theme.FoodScanTheme
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BeverageProductTest {
    @get:Rule(order = 0) val environment = TestEnvironment()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<ComponentActivity>()
    private val juice = Product("juice", "Яблочный сок", "Juices", source = ProductSource.OPEN_FOOD_FACTS,
        categoryTags = listOf("en:beverages", "en:fruit-juices"),
        nutrition = Nutrition(basis = NutritionBasis.PER_100_ML, energyKcal = 45.0, sugarGrams = 10.0,
            carbohydrateGrams = 10.0, fatGrams = 0.0, saturatedFatGrams = 0.0, saltGrams = 0.0))
    @Before fun keepAwake() {
        compose.runOnUiThread { compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    @Test fun juiceShowsBeverageModelWithoutMissingProteinAndFiber() {
        compose.setContent { FoodScanTheme { ProductScreen(juice, {}) } }
        compose.onNodeWithText("Модель: Напитки").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("43 / 100").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Учтено показателей: 6 из 6").performScrollTo().assertIsDisplayed()
        screenshot("juice-score")
        compose.onNodeWithText("Почему такая оценка").performScrollTo().performClick()
        compose.onNodeWithText("Белки и пищевые волокна не влияют на оценку напитков.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Сахар: 10 г").performScrollTo().assertIsDisplayed()
    }
    @Test fun beverageModelFits320DpAnd200PercentFont() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                FoodScanTheme { Box(Modifier.width(320.dp)) { ProductScreen(juice, {}) } }
            }
        }
        compose.onNodeWithText("Модель: Напитки").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Учтено показателей: 6 из 6").performScrollTo().assertIsDisplayed()
        screenshot("juice-large-font")
    }
    @Test fun alcoholWithMissingCategoryShowsExclusionInsteadOfNumericScore() {
        val beer = juice.copy(name = "Пиво", category = null, categoryTags = emptyList(),
            nutrition = juice.nutrition.copy(alcoholPercent = 5.0))
        compose.setContent { FoodScanTheme { ProductScreen(beer, {}) } }
        compose.onNodeWithText("Оценка не применяется").performScrollTo().assertIsDisplayed()
        screenshot("alcohol-excluded")
    }
    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "foodscan-v4-screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
