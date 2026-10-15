package com.foodscan.app.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.foodscan.app.R

/** The four top-level tabs. Product details have a separate route with an ID argument. */
enum class FoodScanDestination(
    val route: String,
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
) {
    Home("home", R.string.nav_home, R.drawable.ic_home),
    Scanner("scanner", R.string.nav_scanner, R.drawable.ic_scan),
    Favorites("favorites", R.string.nav_favorites, R.drawable.ic_favorite),
    Profile("profile", R.string.nav_profile, R.drawable.ic_profile),
}
