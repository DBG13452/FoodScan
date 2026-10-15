package com.foodscan.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foodscan.app.R

@Composable
fun FoodScanBackButton(onBack: () -> Unit) {
    FoodScanButton(stringResource(R.string.product_back), onBack, style = FoodScanButtonStyle.Text,
        leadingIcon = { Icon(painterResource(R.drawable.ic_arrow_back), null, Modifier.size(24.dp)) })
}
