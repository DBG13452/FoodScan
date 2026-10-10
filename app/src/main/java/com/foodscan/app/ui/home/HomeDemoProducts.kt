package com.foodscan.app.ui.home

import androidx.annotation.StringRes
import com.foodscan.app.R

/** Presentation-only examples, not a domain Product or a real browsing history. */
data class HomeDemoProduct(@param:StringRes val name: Int, @param:StringRes val category: Int)

val homeDemoProducts = listOf(
    HomeDemoProduct(R.string.demo_oatmeal, R.string.demo_oatmeal_category),
    HomeDemoProduct(R.string.demo_yogurt, R.string.demo_yogurt_category),
    HomeDemoProduct(R.string.demo_bread, R.string.demo_bread_category),
)
