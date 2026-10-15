package com.foodscan.app.ui.product

import java.math.BigDecimal

/** Russian display formatting without rounding small nonzero measurements to zero. */
internal fun formatNutrient(value: Double?, unit: String, missing: String): String {
    if (value == null || !value.isFinite() || value < 0) return missing
    val number = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString().replace('.', ',')
    return "$number $unit"
}
