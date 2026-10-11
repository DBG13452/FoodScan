package com.foodscan.app.ui.scanner

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface
import com.foodscan.app.R
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanEmptyState
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanScreenTitle
import com.foodscan.app.ui.theme.FoodScanTheme

@Composable
fun ScannerScreen(onHome: () -> Unit, modifier: Modifier = Modifier) {
    FoodScanScreenColumn(modifier) {
        FoodScanScreenTitle(stringResource(R.string.scanner_title))
        FoodScanEmptyState(R.drawable.ic_scan,
            stringResource(R.string.scanner_empty_title),
            stringResource(R.string.scanner_empty_message)) {
            FoodScanButton(stringResource(R.string.back_home), onHome, Modifier.fillMaxWidth(),
                style = FoodScanButtonStyle.Secondary)
        }
    }
}

@Preview(name = "Scanner", widthDp = 360, locale = "ru")
@Preview(name = "Scanner • 200%", widthDp = 320, fontScale = 2f, locale = "ru")
@Composable
private fun ScannerPreview() { FoodScanTheme { Surface { ScannerScreen({}) } } }
