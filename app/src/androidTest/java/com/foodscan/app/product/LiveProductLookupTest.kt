package com.foodscan.app.product

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foodscan.app.ui.FoodScanApp
import com.foodscan.app.ui.theme.FoodScanTheme
import java.io.File
import org.junit.Assume.assumeTrue
import com.foodscan.app.TestEnvironment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Explicit opt-in: exercises a real public API once, separate from deterministic tests. */
@RunWith(AndroidJUnit4::class)
class LiveProductLookupTest {
    @get:Rule(order = 0) val environment = TestEnvironment()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun manualBarcodeLoadsRealProduct() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("liveApi") == "true")
        compose.runOnUiThread { compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
        compose.setContent { FoodScanTheme { FoodScanApp(services = environment.services) } }
        compose.onNodeWithText("Сканировать продукт").performClick()
        compose.onNodeWithText("Штрихкод").performTextReplacement("3017620422003")
        compose.onNodeWithText("Найти продукт").performScrollTo().performClick()
        compose.waitUntil(35_000) {
            compose.onAllNodesWithText("Nutella", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Назад").assertIsDisplayed()
        compose.waitUntil(30_000) {
            compose.onAllNodesWithText("Загружаем фото…").fetchSemanticsNodes().isEmpty()
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.getExternalFilesDir(null), "live-product.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText("Open Food Facts").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Пищевая ценность").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Назад").performScrollTo().performClick()
        compose.onNodeWithText("Штрихкод").assertIsDisplayed()
    }
}
