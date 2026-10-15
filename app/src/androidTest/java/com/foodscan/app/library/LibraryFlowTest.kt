package com.foodscan.app.library

import android.view.WindowManager
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.assertTextContains
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.foodscan.app.TestEnvironment
import com.foodscan.app.waitForText
import com.foodscan.app.data.product.DemoProductRepository
import com.foodscan.app.ui.FoodScanApp
import com.foodscan.app.ui.theme.FoodScanTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import com.foodscan.app.domain.model.NutritionScenario
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryFlowTest {
    @get:Rule(order = 0) val environment = TestEnvironment()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<ComponentActivity>()
    @Before fun prepare() {
        compose.runOnUiThread { compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
        runBlocking { environment.services.library.recordView(DemoProductRepository.products.first(), true) }
    }
    private fun tab(name: String) = compose.onNode(hasText(name) and hasClickAction()).performClick()

    @Test fun favoritesScenarioAndHistoryWorkTogether() {
        compose.setContent { FoodScanTheme { FoodScanApp(services = environment.services) } }
        compose.waitForText("Овсяные хлопья")
        compose.onNodeWithText("Овсяные хлопья").performScrollTo().performClick()
        compose.waitForText("В избранное")
        compose.onNodeWithText("В избранное").performClick()
        compose.waitForText("Убрать из избранного")
        tab("Профиль")
        compose.onNodeWithText("Спорт").performScrollTo().performClick()
        runBlocking { environment.services.settings.scenario.first { it == NutritionScenario.SPORT } }
        compose.waitForIdle()
        compose.onNodeWithText("Спорт").assertIsSelected()
        screenshot("profile")
        tab("Избранное")
        compose.waitForText("Овсяные хлопья")
        screenshot("favorites")
        compose.onNodeWithText("Овсяные хлопья").performClick()
        compose.waitForText("Убрать из избранного")
        compose.onNodeWithText("Спорт").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Почему такая оценка").performScrollTo().performClick()
        compose.onAllNodesWithText("Вклад:", substring = true)[0].performScrollTo().assertIsDisplayed()
        screenshot("score-explanation")
        tab("Профиль")
        compose.onNodeWithText("Вся история").performScrollTo().performClick()
        compose.waitForText("Очистить историю")
        screenshot("history")
        compose.onNodeWithText("Очистить историю").performClick()
        compose.onNodeWithText("Удалить").performClick()
        compose.waitForText("История пока пуста")
        tab("Избранное")
        compose.onNodeWithText("Овсяные хлопья").assertIsDisplayed()
        tab("Профиль")
        compose.onNodeWithText("Удалить мои данные").performScrollTo().performClick()
        compose.onNodeWithText("Удалить").performClick()
        tab("Избранное")
        compose.waitForText("Пока ничего не сохранено")
    }

    @Test fun dictionaryOpensAndReturnsWithoutChangingTabs() {
        compose.setContent { FoodScanTheme { FoodScanApp(services = environment.services) } }
        tab("Профиль")
        compose.onNodeWithText("Справочник добавок").performScrollTo().performClick()
        compose.onNodeWithTag("additive-search").performTextReplacement("Е 330")
        compose.onNodeWithText("E330  Лимонная кислота").performScrollTo().performClick()
        compose.onNodeWithText("Регулятор кислотности").assertIsDisplayed()
        screenshot("additive")
        compose.onNodeWithText("Назад").performScrollTo().performClick()
        compose.onNodeWithText("Справочник добавок").assertIsDisplayed()
        compose.onNodeWithTag("additive-search").assertTextContains("Е 330")
        compose.onNodeWithTag("additive-search").performTextReplacement("E9999")
        compose.onNodeWithText("Ничего не найдено. Попробуйте E-код или другое название.").assertIsDisplayed()
        compose.onNodeWithText("Сбросить поиск").performClick()
        compose.onNodeWithText("E100  Куркумин").assertIsDisplayed()
    }

    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "stage12-screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
