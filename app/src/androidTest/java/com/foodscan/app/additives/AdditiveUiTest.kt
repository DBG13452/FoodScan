package com.foodscan.app.additives

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
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
import com.foodscan.app.data.product.DemoProductRepository
import com.foodscan.app.ui.additives.AdditiveScreen
import com.foodscan.app.ui.product.ProductScreen
import com.foodscan.app.ui.theme.FoodScanTheme
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdditiveUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Before fun keepAwake() {
        compose.runOnUiThread { compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    @Test fun productLabelsAllCategoriesAndOpensTheSelectedDescription() {
        compose.setContent {
            var selected by remember { mutableStateOf<String?>(null) }
            FoodScanTheme {
                if (selected != null) AdditiveScreen(selected, { selected = null }, { selected = it })
                else ProductScreen(DemoProductRepository.products.first().copy(
                    additives = listOf("E128", "E250", "E330", "E9999")), {}, onAdditive = { selected = it })
            }
        }
        compose.onNodeWithText("E128  Красный 2G").performScrollTo().assertTextContains("Не разрешена в РФ/ЕАЭС")
        compose.onNodeWithText("E250  Нитрит натрия").performScrollTo().assertTextContains("Стоит обратить внимание")
        compose.onNodeWithText("E330  Лимонная кислота").performScrollTo().assertTextContains("Низкая обеспокоенность")
        compose.onNodeWithText("E9999").performScrollTo().assertTextContains("Нет проверенной оценки")
        screenshot("product-additives")
        compose.onNodeWithText("E250  Нитрит натрия").performScrollTo().performClick()
        compose.onNodeWithText("Возможно образование нитрозосоединений").assertIsDisplayed()
        compose.onNodeWithText("Влияние на организм").performScrollTo().assertIsDisplayed()
        screenshot("nitrite-details")
        compose.onNodeWithText("Назад").performScrollTo().performClick()
        compose.onNodeWithText("E9999").performScrollTo().performClick()
        compose.onNodeWithText("Нет проверенной оценки").assertIsDisplayed()
    }

    @Test fun longDescriptionAndSourcesRemainReachableAtLargeFont() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                FoodScanTheme { Box(Modifier.width(320.dp)) { AdditiveScreen("E951", {}, {}) } }
            }
        }
        compose.onNodeWithText("Влияние на организм").performScrollTo().assertIsDisplayed()
        screenshot("aspartame-large-font")
        compose.onNodeWithText("Источники на русском").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Проверено: 15.10.2026").performScrollTo().assertIsDisplayed()
    }

    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "additives-screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
