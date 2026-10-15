package com.foodscan.app.ui.additives

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.foodscan.app.R
import com.foodscan.app.data.additives.AdditiveConcern
import com.foodscan.app.data.additives.AdditiveDictionary
import com.foodscan.app.ui.components.FoodScanBackButton
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanCard
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanScreenTitle
import com.foodscan.app.ui.components.FoodScanSectionTitle
import com.foodscan.app.ui.theme.FoodScanSpacing
import com.foodscan.app.ui.theme.FoodScanTheme

@Composable
fun AdditiveScreen(code: String?, onBack: () -> Unit, onOpen: (String) -> Unit) {
    if (code == null) AdditiveList(onBack, onOpen) else AdditiveDetails(code, onBack)
}

@Composable
private fun AdditiveList(onBack: () -> Unit, onOpen: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) { AdditiveDictionary.search(query) }
    val keyboard = LocalSoftwareKeyboardController.current
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding(),
        contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 640.dp).fillMaxWidth().testTag("additive-list"),
            contentPadding = PaddingValues(FoodScanSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.medium)) {
            item(key = "back") { FoodScanBackButton(onBack) }
            item(key = "title") { FoodScanScreenTitle(stringResource(R.string.additives_dictionary)) }
            item(key = "search") {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().testTag("additive-search"),
                    label = { Text(stringResource(R.string.additive_search)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }))
            }
            item(key = "count") { Text(stringResource(R.string.additive_count, results.size, AdditiveDictionary.entries.size),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (results.isEmpty()) item(key = "empty") {
                Text(stringResource(R.string.additive_no_results), style = MaterialTheme.typography.bodyLarge)
                FoodScanButton(stringResource(R.string.additive_clear_search), { query = "" }, style = FoodScanButtonStyle.Text)
            }
            items(results, key = { it.code }) { info ->
                AdditiveRow(info.code) { keyboard?.hide(); onOpen(info.code) }
            }
        }
    }
}

@Composable
private fun AdditiveDetails(code: String, onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val normalized = AdditiveDictionary.normalize(code)
    val info = AdditiveDictionary.find(code)
    val concern = info?.concern ?: AdditiveConcern.UNKNOWN
    FoodScanScreenColumn {
        FoodScanBackButton(onBack)
        FoodScanScreenTitle(normalized, info?.name)
        Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = concern.container()) {
            Column(Modifier.padding(FoodScanSpacing.large), verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.small)) {
                Text(concern.label(), style = MaterialTheme.typography.titleMedium, color = concern.foreground())
                Text(info?.reason ?: stringResource(R.string.additive_unknown), style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (info != null) {
            FoodScanCard(Modifier.fillMaxWidth()) {
                Text(info.purpose, style = MaterialTheme.typography.titleMedium)
                Text(info.description, style = MaterialTheme.typography.bodyLarge)
                if (normalized != info.code) Text(stringResource(R.string.additive_group, info.code),
                    style = MaterialTheme.typography.bodySmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.small)) {
                FoodScanSectionTitle(stringResource(R.string.additive_health))
                Text(info.healthEffects, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Text(stringResource(R.string.additive_note), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (info != null) {
            Column(verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.small)) {
                FoodScanSectionTitle(stringResource(R.string.additive_sources))
                info.sources.forEach { source ->
                    FoodScanButton(source.title, { uriHandler.openUri(source.url) },
                        style = FoodScanButtonStyle.Text, modifier = Modifier.fillMaxWidth())
                }
                Text(stringResource(R.string.additive_reviewed, AdditiveDictionary.reviewedOn),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 800, locale = "ru")
@Composable
private fun AdditiveListPreview() { FoodScanTheme { AdditiveScreen(null, {}, {}) } }

@Preview(widthDp = 320, heightDp = 800, fontScale = 2f, locale = "ru")
@Composable
private fun AdditiveDetailsPreview() { FoodScanTheme { AdditiveScreen("E250", {}, {}) } }
