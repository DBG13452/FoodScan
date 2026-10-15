package com.foodscan.app.scanner

import android.graphics.Bitmap
import android.graphics.Color
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.foodscan.app.ui.scanner.ScannerScreen
import com.foodscan.app.ui.theme.FoodScanTheme
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ScannerFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @Before fun keepScreenAwake() {
        compose.runOnUiThread { compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    @Test fun manualEntryValidatesAndDispatchesOnlyOnce() {
        val opened = mutableListOf<String>()
        compose.setContent { FoodScanTheme { ScannerScreen({}, { opened.add(it) }) } }
        compose.onNodeWithText("Штрихкод").performTextReplacement("123")
        compose.onNodeWithText("Найти продукт").performScrollTo().performClick()
        assertTrue(opened.isEmpty())
        compose.onNodeWithText("Проверьте цифры штрихкода", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Штрихкод").performTextReplacement("3017620422003")
        compose.onNodeWithText("Найти продукт").performScrollTo().performClick()
        compose.onNodeWithText("Найти продукт").performClick()
        assertEquals(listOf("3017620422003"), opened)
    }

    @Test fun bundledMlKitRecognizesAnEan13Image() {
        val left = listOf("0001101", "0011001", "0010011", "0111101", "0100011", "0110001", "0101111", "0111011", "0110111", "0001011")
        val even = listOf("0100111", "0110011", "0011011", "0100001", "0011101", "0111001", "0000101", "0010001", "0001001", "0010111")
        val code = "3017620422003"
        val parity = "LLGGGL" // Leading EAN digit 3.
        val modules = "101" + code.substring(1, 7).mapIndexed { i, digit ->
            (if (parity[i] == 'L') left else even)[digit - '0']
        }.joinToString("") + "01010" + code.substring(7).map { digit ->
            left[digit - '0'].map { if (it == '1') '0' else '1' }.joinToString("")
        }.joinToString("") + "101"
        val bitmap = Bitmap.createBitmap((modules.length + 24) * 4, 240, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        modules.forEachIndexed { index, bit ->
            if (bit == '1') for (x in (index + 12) * 4 until (index + 13) * 4)
                for (y in 20 until 220) bitmap.setPixel(x, y, Color.BLACK)
        }
        val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_EAN_13).build())
        try {
            val results = Tasks.await(scanner.process(InputImage.fromBitmap(bitmap, 0)), 15, TimeUnit.SECONDS)
            assertTrue(results.any { it.rawValue == code })
        } finally {
            scanner.close()
            bitmap.recycle()
        }
    }
}
