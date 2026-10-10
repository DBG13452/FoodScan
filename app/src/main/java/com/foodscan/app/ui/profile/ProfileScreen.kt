package com.foodscan.app.ui.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.foodscan.app.R
import com.foodscan.app.ui.components.FoodScanCard
import com.foodscan.app.ui.components.FoodScanMessage
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanSectionTitle

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    FoodScanScreenColumn(modifier) {
        FoodScanSectionTitle(stringResource(R.string.profile_title))
        FoodScanCard(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.scenario_title), style = MaterialTheme.typography.labelLarge)
            Text(
                stringResource(R.string.scenario_normal),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        FoodScanMessage(
            title = stringResource(R.string.profile_placeholder_title),
            message = stringResource(R.string.profile_placeholder_message),
            modifier = Modifier.fillMaxWidth(),
        )
        FoodScanCard(Modifier.fillMaxWidth()) {
            FoodScanSectionTitle(stringResource(R.string.about_title))
            Text(stringResource(R.string.about_message), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
