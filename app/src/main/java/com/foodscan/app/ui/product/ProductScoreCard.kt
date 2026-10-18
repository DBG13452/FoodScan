package com.foodscan.app.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.foodscan.app.R
import com.foodscan.app.domain.model.NutritionScenario
import com.foodscan.app.domain.model.Product
import com.foodscan.app.domain.scoring.ProductScore
import com.foodscan.app.domain.scoring.ScoreFactor
import com.foodscan.app.domain.scoring.ScoreProblem
import com.foodscan.app.domain.scoring.ScoreProfile
import com.foodscan.app.domain.scoring.ScoringEngine
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanCard
import com.foodscan.app.ui.components.FoodScanSectionTitle
import com.foodscan.app.ui.theme.FoodScanSpacing
import com.foodscan.app.ui.theme.FoodScanColors
import kotlin.math.round

@Composable
fun scenarioLabel(scenario: NutritionScenario): String = stringResource(when (scenario) {
    NutritionScenario.NORMAL -> R.string.scenario_normal
    NutritionScenario.SPORT -> R.string.scenario_sport
    NutritionScenario.WEIGHT_CONTROL -> R.string.scenario_weight
    NutritionScenario.SUGAR_CONTROL -> R.string.scenario_sugar
})

@Composable
private fun factorLabel(factor: ScoreFactor): String = stringResource(when (factor) {
    ScoreFactor.SUGAR -> R.string.product_sugar
    ScoreFactor.SATURATED_FAT -> R.string.product_saturated_fat
    ScoreFactor.SALT -> R.string.product_salt
    ScoreFactor.FIBER -> R.string.product_fiber
    ScoreFactor.PROTEIN -> R.string.product_protein
    ScoreFactor.ENERGY -> R.string.product_energy
    ScoreFactor.CARBOHYDRATES -> R.string.product_carbohydrate
    ScoreFactor.FAT -> R.string.product_fat
})

@Composable
fun ProductScoreCard(product: Product, scenario: NutritionScenario, onMethodology: () -> Unit) {
    val score = remember(product, scenario) { ScoringEngine.evaluate(product, scenario) }
    var expanded by rememberSaveable(product.id, scenario) { mutableStateOf(false) }
    FoodScanCard(Modifier.fillMaxWidth()) {
        FoodScanSectionTitle(stringResource(R.string.product_score))
        when (score) {
            is ProductScore.Rated -> {
                val profileName = stringResource(if (score.profile == ScoreProfile.BEVERAGE)
                    R.string.score_profile_beverage else R.string.score_profile_food)
                Text(stringResource(R.string.score_profile, profileName), style = MaterialTheme.typography.titleMedium)
                if (score.preliminary) Text(stringResource(R.string.score_preliminary),
                    style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${score.value} / 100", style = MaterialTheme.typography.headlineLarge,
                    color = when {
                        score.value < 40 -> MaterialTheme.colorScheme.error
                        score.value < 70 -> FoodScanColors.warningText
                        else -> FoodScanColors.successText
                    })
                Text(stringResource(R.string.score_coverage, score.contributions.size, score.totalFactors),
                    style = MaterialTheme.typography.titleMedium)
                if (score.assumedProfile) Text(stringResource(R.string.score_assumed_profile),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (score.missing.isNotEmpty()) {
                    Text(stringResource(R.string.score_missing, score.missing.map { factorLabel(it) }.joinToString(", ")),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FoodScanButton(stringResource(if (expanded) R.string.score_hide_details else R.string.score_explain),
                    { expanded = !expanded }, style = FoodScanButtonStyle.Text)
                if (expanded) {
                    if (score.profile == ScoreProfile.BEVERAGE) Text(stringResource(R.string.score_beverage_explanation),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (score.preliminary) {
                        Text(stringResource(R.string.score_partial_explanation, score.coveredWeight),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    score.contributions.forEach { contribution ->
                        val unit = stringResource(if (contribution.factor == ScoreFactor.ENERGY) R.string.product_kcal else R.string.product_grams)
                        val actual = formatNutrient(contribution.value, unit, "")
                        val points = formatNutrient(round(score.normalizedPoints(contribution) * 10) / 10, "", "").trim()
                        Column(verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.extraSmall)) {
                            Text("${factorLabel(contribution.factor)}: $actual", style = MaterialTheme.typography.titleLarge)
                            val weight = formatNutrient(round(score.normalizedWeight(contribution) * 10) / 10, "", "").trim()
                            Text(stringResource(R.string.score_points_v2, points, weight), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            is ProductScore.Insufficient -> {
                Text(stringResource(if (score.problem == ScoreProblem.EXCLUDED_CATEGORY)
                    R.string.score_not_applicable else R.string.score_insufficient), style = MaterialTheme.typography.titleMedium)
                Text(if (score.missing.isNotEmpty()) stringResource(R.string.score_missing,
                    score.missing.map { factorLabel(it) }.joinToString(", ")) else stringResource(when (score.problem) {
                    ScoreProblem.UNKNOWN_BASIS -> R.string.score_unknown_basis
                    ScoreProblem.UNKNOWN_CATEGORY -> R.string.score_unknown_category
                    ScoreProblem.EXCLUDED_CATEGORY -> R.string.score_excluded_category
                    ScoreProblem.INVALID_DATA -> R.string.score_invalid_data
                    ScoreProblem.MISSING_DATA -> R.string.score_insufficient
                }), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(stringResource(R.string.scenario_title), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(scenarioLabel(scenario), style = MaterialTheme.typography.titleMedium)
        FoodScanButton(stringResource(R.string.score_methodology), onMethodology, style = FoodScanButtonStyle.Text)
    }
}
