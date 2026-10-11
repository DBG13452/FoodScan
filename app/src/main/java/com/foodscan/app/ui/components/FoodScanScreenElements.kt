package com.foodscan.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.foodscan.app.ui.theme.FoodScanSpacing

@Composable
fun FoodScanScreenTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Decorative barcode, deliberately not a real or scannable product code. */
@Composable
fun FoodScanBarcode(modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onPrimary
    val accent = MaterialTheme.colorScheme.inversePrimary
    Canvas(modifier) {
        val unit = size.width / 100f
        val top = size.height * .2f
        val bottom = size.height * .8f
        val bars = listOf(2, 1, 3, 1, 2, 4, 1, 2, 1, 3, 2, 1, 4, 2, 1, 2, 3)
        var x = 12f
        bars.forEach { width ->
            drawRect(ink, Offset(x * unit, top), Size(width * unit, bottom - top))
            x += width + 2f
        }
        val corner = 9 * unit
        listOf(Offset(0f, 0f), Offset(size.width, 0f), Offset(0f, size.height), Offset(size.width, size.height)).forEach { p ->
            val dx = if (p.x == 0f) corner else -corner
            val dy = if (p.y == 0f) corner else -corner
            drawLine(accent, p, Offset(p.x + dx, p.y), 3.dp.toPx(), StrokeCap.Round)
            drawLine(accent, p, Offset(p.x, p.y + dy), 3.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
fun FoodScanEmptyState(
    @DrawableRes icon: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Column(modifier.fillMaxWidth().padding(vertical = FoodScanSpacing.section),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.large)) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(painterResource(icon), null, Modifier.padding(24.dp).size(40.dp),
                tint = MaterialTheme.colorScheme.primary)
        }
        Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center)
        Text(message, Modifier.widthIn(max = 360.dp), style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        action()
    }
}
