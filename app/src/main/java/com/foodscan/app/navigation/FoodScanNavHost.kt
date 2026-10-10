package com.foodscan.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.foodscan.app.ui.favorites.FavoritesScreen
import com.foodscan.app.ui.home.HomeScreen
import com.foodscan.app.ui.profile.ProfileScreen
import com.foodscan.app.ui.scanner.ScannerScreen

internal fun NavHostController.navigateTo(destination: FoodScanDestination) {
    if (currentDestination?.route == destination.route) return
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun FoodScanNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = FoodScanDestination.Home.route,
        modifier = modifier,
    ) {
        composable(FoodScanDestination.Home.route) {
            HomeScreen(
                onScan = { navController.navigateTo(FoodScanDestination.Scanner) },
                onFavorites = { navController.navigateTo(FoodScanDestination.Favorites) },
                onProfile = { navController.navigateTo(FoodScanDestination.Profile) },
            )
        }
        composable(FoodScanDestination.Scanner.route) {
            ScannerScreen(onHome = { navController.navigateTo(FoodScanDestination.Home) })
        }
        composable(FoodScanDestination.Favorites.route) {
            FavoritesScreen(onHome = { navController.navigateTo(FoodScanDestination.Home) })
        }
        composable(FoodScanDestination.Profile.route) { ProfileScreen() }
    }
}
