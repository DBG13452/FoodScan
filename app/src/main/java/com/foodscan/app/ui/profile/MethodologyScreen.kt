package com.foodscan.app.ui.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.foodscan.app.R
import com.foodscan.app.domain.scoring.ScoringEngine
import com.foodscan.app.ui.components.FoodScanBackButton
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanScreenTitle
import com.foodscan.app.ui.components.FoodScanSectionTitle

@Composable
fun MethodologyScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    FoodScanScreenColumn {
        FoodScanBackButton(onBack)
        FoodScanScreenTitle(stringResource(R.string.score_methodology), ScoringEngine.VERSION)
        Text(stringResource(R.string.method_intro), style = MaterialTheme.typography.bodyLarge)
        FoodScanSectionTitle(stringResource(R.string.method_thresholds))
        Text(stringResource(R.string.method_thresholds_text), style = MaterialTheme.typography.bodyLarge)
        FoodScanSectionTitle(stringResource(R.string.method_weights))
        Text(stringResource(R.string.method_weights_text), style = MaterialTheme.typography.bodyLarge)
        FoodScanSectionTitle(stringResource(R.string.method_completeness))
        Text(stringResource(R.string.method_completeness_text), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.method_limits), style = MaterialTheme.typography.bodyMedium)
        FoodScanButton(stringResource(R.string.method_source), {
            uriHandler.openUri("https://www.gov.uk/government/publications/front-of-pack-nutrition-labelling-guidance")
        }, style = FoodScanButtonStyle.Text)
    }
}
