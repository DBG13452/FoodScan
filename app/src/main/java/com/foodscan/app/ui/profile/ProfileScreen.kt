package com.foodscan.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.foodscan.app.R
import com.foodscan.app.ui.components.FoodScanCard
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanScreenTitle
import com.foodscan.app.ui.theme.FoodScanSpacing
import com.foodscan.app.ui.theme.FoodScanTheme

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    FoodScanScreenColumn(modifier) {
        FoodScanScreenTitle(stringResource(R.string.profile_title))
        FoodScanCard(Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(FoodScanSpacing.medium),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(painterResource(R.drawable.ic_profile), null, Modifier.padding(12.dp).size(24.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.scenario_title), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.scenario_normal), style = MaterialTheme.typography.titleMedium)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Text(stringResource(R.string.profile_placeholder_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.profile_placeholder_message), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(name = "Profile", widthDp = 360, locale = "ru")
@Preview(name = "Profile • 200%", widthDp = 320, fontScale = 2f, locale = "ru")
@Composable
private fun ProfilePreview() { FoodScanTheme { Surface { ProfileScreen() } } }
