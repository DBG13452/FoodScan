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
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foodscan.app.data.product.DemoProductRepository
import com.foodscan.app.navigation.FoodScanDestination
import com.foodscan.app.navigation.FoodScanNavHost
import com.foodscan.app.navigation.ProductDestination
import com.foodscan.app.navigation.navigateTo
import com.foodscan.app.ui.product.ProductScreen
import com.foodscan.app.ui.theme.FoodScanTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Before
import com.foodscan.app.TestEnvironment
import com.foodscan.app.waitForText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductScreenTest {
    @get:Rule(order = 0) val environment = TestEnvironment()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun keepTestScreenAwake() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun productsOpenRestoreAndReturnToHome() {
        lateinit var controller: NavHostController
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            FoodScanTheme {
                controller = rememberNavController()
                FoodScanNavHost(controller, services = environment.services, homeExamples = DemoProductRepository.products)
            }
        }
        DemoProductRepository.products.forEach { product ->
            compose.onNodeWithText(product.name).performScrollTo().performClick()
            compose.waitForText("Демонстрационный продукт")
            compose.onNodeWithText("Демонстрационный продукт").assertIsDisplayed()
            compose.runOnIdle {
                assertEquals(product.id, controller.currentBackStackEntry?.arguments?.getString(ProductDestination.argument))
            }
            restoration.emulateSavedInstanceStateRestore()
            compose.waitForText("Демонстрационный продукт")
            compose.onNodeWithText(product.name).assertIsDisplayed()
            compose.onNodeWithText("Назад").performClick()
            compose.runOnIdle { assertEquals("home", controller.currentDestination?.route) }
        }
        compose.onNodeWithText("Цельнозерновой хлеб").performScrollTo().performClick()
        compose.runOnIdle { controller.navigateTo(FoodScanDestination.Favorites) }
        compose.onNodeWithText("Пока ничего не сохранено").assertIsDisplayed()
        compose.runOnIdle { controller.navigateTo(FoodScanDestination.Home) }
        compose.onNodeWithText("Сканировать продукт").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun nutritionDistinguishesZeroFromMissingAndAllSectionsAreReachable() {
        lateinit var controller: NavHostController
        compose.setContent {
            FoodScanTheme {
                controller = rememberNavController()
                FoodScanNavHost(controller, services = environment.services, homeExamples = DemoProductRepository.products)
            }
        }
        compose.onNodeWithText("Овсяные хлопья").performScrollTo().performClick()
        compose.waitForText("Демонстрационный продукт")
        screenshot("product-top")
        compose.onNodeWithTag("nutrition-salt").performScrollTo().assertTextContains("0 г")
        screenshot("product-nutrition")
        compose.onNodeWithText("Добавки").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Не заявлены в источнике").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Назад").performScrollTo().performClick()
        compose.onNodeWithText("Цельнозерновой хлеб").performScrollTo().performClick()
        compose.waitForText("Демонстрационный продукт")
        compose.onNodeWithTag("nutrition-salt").performScrollTo().assertTextContains("Нет данных")
        compose.onNodeWithTag("nutrition-energy").performScrollTo().assertTextContains("Нет данных")
    }

    @Test
    fun narrowScreenWithLargeFontCanReachTheLastSection() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                FoodScanTheme {
                    Box(Modifier.width(320.dp)) {
                        ProductScreen(DemoProductRepository.products.first().copy(
                            name = "Овсяные хлопья цельнозерновые с длинным названием продукта",
                        ), {})
                    }
                }
            }
        }
        screenshot("product-narrow-large-font")
        compose.onNodeWithTag("nutrition-saturated-fat").performScrollTo().assertTextContains("1,2 г")
        screenshot("product-narrow-nutrition")
        compose.onNodeWithText("Не заявлены в источнике").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Назад").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun unknownProductShowsRecoveryInsteadOfCrashing() {
        lateinit var controller: NavHostController
        compose.setContent {
            FoodScanTheme {
                controller = rememberNavController()
                FoodScanNavHost(controller, services = environment.services, homeExamples = DemoProductRepository.products)
            }
        }
        compose.runOnIdle { controller.navigate(ProductDestination.routeFor("missing-product")) }
        compose.waitForText("Продукт не найден")
        compose.onNodeWithText("Продукт не найден").assertIsDisplayed()
        compose.onNodeWithText("Назад").performClick()
        compose.onNodeWithText("FoodScan").assertIsDisplayed()
    }

    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "product-screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
