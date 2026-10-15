package com.foodscan.app.navigation

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.foodscan.app.ui.theme.FoodScanTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import com.foodscan.app.data.product.DemoProductRepository
import com.foodscan.app.TestEnvironment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TabSwitchingTest {
    @get:Rule(order = 0) val environment = TestEnvironment()
    @get:Rule(order = 1) val compose = createComposeRule()

    @Test
    fun rapidSwitchesPaintOnlyTheSelectedScreen() {
        lateinit var controller: NavHostController
        compose.setContent {
            FoodScanTheme {
                controller = rememberNavController()
                FoodScanNavHost(controller, services = environment.services, homeExamples = DemoProductRepository.products)
            }
        }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        val references = FoodScanDestination.entries.associateWith { destination ->
            compose.runOnUiThread { controller.navigateTo(destination) }
            compose.mainClock.advanceTimeBy(500)
            compose.onRoot().captureToImage().asAndroidBitmap()
        }
        // Interrupt navigation every frame, including several changes within one frame.
        repeat(24) { index ->
            val destination = FoodScanDestination.entries[index % 4]
            compose.runOnUiThread {
                controller.navigateTo(FoodScanDestination.entries[(index + 1) % 4])
                controller.navigateTo(destination)
                controller.navigateTo(destination)
            }
            compose.mainClock.advanceTimeByFrame()
            compose.mainClock.advanceTimeByFrame()
            val actual = compose.onRoot().captureToImage().asAndroidBitmap()
            assertTrue("Overlapping or incomplete screen: $destination, iteration $index",
                references.getValue(destination).sameAs(actual))
            compose.runOnUiThread {
                assertEquals(destination.route, controller.currentDestination?.route)
            }
        }
        compose.runOnUiThread { controller.popBackStack() }
        compose.mainClock.advanceTimeBy(500)
        compose.runOnUiThread {
            assertEquals(FoodScanDestination.Home.route, controller.currentDestination?.route)
        }
    }
}
