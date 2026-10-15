package com.foodscan.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.foodscan.app.R
import com.foodscan.app.data.local.ViewedProduct
import com.foodscan.app.domain.model.Product
import com.foodscan.app.ui.components.FoodScanBackButton
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanCard
import com.foodscan.app.ui.components.FoodScanEmptyState
import com.foodscan.app.ui.components.FoodScanScreenTitle
import com.foodscan.app.ui.theme.FoodScanSpacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun LibraryProductRow(product: Product, subtitle: String?, onOpen: () -> Unit, onRemove: () -> Unit, removeLabel: String, busy: Boolean) {
    FoodScanCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FoodScanSpacing.small)) {
            Column(Modifier.weight(1f).heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onOpen)
                .padding(vertical = FoodScanSpacing.small), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRemove, enabled = !busy) {
                Icon(painterResource(R.drawable.ic_delete), removeLabel, Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun HistoryScreen(
    entries: List<ViewedProduct>, onBack: () -> Unit, onOpen: (String) -> Unit,
    onRemove: (Long) -> Unit, onClear: () -> Unit, busy: Boolean,
) {
    var clearConfirmation by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 640.dp).fillMaxWidth(),
            contentPadding = PaddingValues(FoodScanSpacing.screen), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { FoodScanBackButton(onBack) }
            item { FoodScanScreenTitle(stringResource(R.string.history_title)) }
            if (entries.isEmpty()) item {
                FoodScanEmptyState(R.drawable.ic_history, stringResource(R.string.history_empty), stringResource(R.string.history_empty_hint))
            } else {
                item { FoodScanButton(stringResource(R.string.history_clear), { clearConfirmation = true },
                    style = FoodScanButtonStyle.Text, enabled = !busy) }
                items(entries, key = { it.eventId }) { entry ->
                    val date = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.forLanguageTag("ru"))
                        .withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(entry.viewedAt))
                    LibraryProductRow(entry.product, date, { onOpen(entry.product.id) }, { onRemove(entry.eventId) },
                        stringResource(R.string.history_remove_item, entry.product.name), busy)
                }
            }
        }
    }
    if (clearConfirmation) ConfirmDeletion(stringResource(R.string.history_clear_question),
        stringResource(R.string.history_clear_explanation), { clearConfirmation = false }, {
            clearConfirmation = false; onClear()
        })
}

@Composable
fun ConfirmDeletion(title: String, explanation: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(explanation) },
        confirmButton = { FoodScanButton(stringResource(R.string.action_delete), onConfirm) },
        dismissButton = { FoodScanButton(stringResource(R.string.action_cancel), onDismiss, style = FoodScanButtonStyle.Text) })
}
