package com.foodscan.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.foodscan.app.R
import com.foodscan.app.domain.model.NutritionScenario
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanCard
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanScreenTitle
import com.foodscan.app.ui.components.FoodScanSectionTitle
import com.foodscan.app.ui.library.ConfirmDeletion
import com.foodscan.app.ui.product.scenarioLabel
import com.foodscan.app.ui.theme.FoodScanSpacing

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    scenario: NutritionScenario = NutritionScenario.NORMAL,
    historyEnabled: Boolean = true, busy: Boolean = false,
    onScenario: (NutritionScenario) -> Unit = {}, onHistoryEnabled: (Boolean) -> Unit = {},
    onHistory: () -> Unit = {}, onAdditives: () -> Unit = {}, onMethodology: () -> Unit = {},
    onDeleteAll: () -> Unit = {},
) {
    var deleteConfirmation by rememberSaveable { mutableStateOf(false) }
    FoodScanScreenColumn(modifier) {
        FoodScanScreenTitle(stringResource(R.string.profile_title))
        FoodScanSectionTitle(stringResource(R.string.scenario_title))
        FoodScanCard(Modifier.fillMaxWidth().selectableGroup()) {
            NutritionScenario.entries.forEach { choice ->
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp)
                    .selectable(choice == scenario, enabled = !busy, role = Role.RadioButton, onClick = { onScenario(choice) })
                    .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FoodScanSpacing.medium)) {
                    RadioButton(selected = choice == scenario, onClick = null, enabled = !busy)
                    Column(Modifier.weight(1f)) {
                        Text(scenarioLabel(choice), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(when (choice) {
                            NutritionScenario.NORMAL -> R.string.scenario_normal_hint
                            NutritionScenario.SPORT -> R.string.scenario_sport_hint
                            NutritionScenario.WEIGHT_CONTROL -> R.string.scenario_weight_hint
                            NutritionScenario.SUGAR_CONTROL -> R.string.scenario_sugar_hint
                        }), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        FoodScanSectionTitle(stringResource(R.string.history_title))
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .toggleable(historyEnabled, enabled = !busy, role = Role.Switch, onValueChange = onHistoryEnabled),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.history_enabled), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = historyEnabled, onCheckedChange = null, enabled = !busy)
        }
        FoodScanButton(stringResource(R.string.history_open), onHistory, Modifier.fillMaxWidth(), style = FoodScanButtonStyle.Secondary)
        FoodScanSectionTitle(stringResource(R.string.profile_information))
        FoodScanButton(stringResource(R.string.additives_dictionary), onAdditives, Modifier.fillMaxWidth(), style = FoodScanButtonStyle.Secondary)
        FoodScanButton(stringResource(R.string.score_methodology), onMethodology, Modifier.fillMaxWidth(), style = FoodScanButtonStyle.Secondary)
        Text(stringResource(R.string.profile_about), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        FoodScanButton(stringResource(R.string.delete_all_data), { deleteConfirmation = true }, Modifier.fillMaxWidth(),
            style = FoodScanButtonStyle.Text, enabled = !busy)
    }
    if (deleteConfirmation) ConfirmDeletion(stringResource(R.string.delete_all_question),
        stringResource(R.string.delete_all_explanation), { deleteConfirmation = false }, {
            deleteConfirmation = false; onDeleteAll()
        })
}
