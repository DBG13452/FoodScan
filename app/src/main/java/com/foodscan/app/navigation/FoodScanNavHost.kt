package com.foodscan.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foodscan.app.R
import com.foodscan.app.data.AppContainer
import com.foodscan.app.data.AppServices
import com.foodscan.app.domain.model.Product
import com.foodscan.app.ui.library.LibraryViewModel
import com.foodscan.app.ui.library.HistoryScreen
import com.foodscan.app.ui.additives.AdditiveScreen
import com.foodscan.app.ui.profile.MethodologyScreen
import com.foodscan.app.ui.components.FoodScanButton
import android.net.Uri
import com.foodscan.app.ui.favorites.FavoritesScreen
import com.foodscan.app.ui.home.HomeScreen
import com.foodscan.app.ui.profile.ProfileScreen
import com.foodscan.app.ui.scanner.ScannerScreen
import com.foodscan.app.ui.product.ProductRoute

internal fun NavHostController.navigateTo(destination: FoodScanDestination) {
    // A tab always opens its root; do not restore an old product instead of the scanner.
    while (FoodScanDestination.entries.none { it.route == currentDestination?.route } && previousBackStackEntry != null) {
        if (!popBackStack()) break
    }
    if (currentDestination?.route == destination.route) return
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun FoodScanNavHost(
    navController: NavHostController, modifier: Modifier = Modifier,
    services: AppContainer = AppServices.get(LocalContext.current),
    homeExamples: List<Product>? = null,
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val library: LibraryViewModel = viewModel(key = "user-library", factory = LibraryViewModel.factory(services))
    val libraryState by library.state.collectAsStateWithLifecycle()
    val busy by library.busy.collectAsStateWithLifecycle()
    val error by library.error.collectAsStateWithLifecycle()
    val openProduct: (String) -> Unit = { id ->
        navController.navigate(ProductDestination.routeFor(id)) { launchSingleTop = true }
    }
    val openAdditive: (String) -> Unit = { code -> navController.navigate("additive/${Uri.encode(code)}") { launchSingleTop = true } }
    if (error) AlertDialog(onDismissRequest = library::dismissError,
        title = { Text(stringResource(R.string.storage_error)) }, text = { Text(stringResource(R.string.storage_error_message)) },
        confirmButton = { FoodScanButton(stringResource(R.string.action_close), library::dismissError) })
    NavHost(
        navController = navController,
        startDestination = FoodScanDestination.Home.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(FoodScanDestination.Home.route) { entry ->
            CurrentDestination(navController, entry) {
                HomeScreen(
                    onScan = { navController.navigateTo(FoodScanDestination.Scanner) },
                    onFavorites = { navController.navigateTo(FoodScanDestination.Favorites) },
                    onProfile = { navController.navigateTo(FoodScanDestination.Profile) },
                    onProduct = openProduct,
                    recentProducts = homeExamples ?: libraryState.history.map { it.product }.distinctBy { it.id }.take(5),
                    scenario = libraryState.scenario,
                    onHistory = { navController.navigate("history") { launchSingleTop = true } },
                )
            }
        }
        composable(FoodScanDestination.Scanner.route) { entry ->
            CurrentDestination(navController, entry) {
                ScannerScreen(
                    onHome = { navController.navigateTo(FoodScanDestination.Home) },
                    onBarcode = openProduct,
                    isActive = currentEntry?.id == entry.id,
                )
            }
        }
        composable(FoodScanDestination.Favorites.route) { entry ->
            CurrentDestination(navController, entry) {
                FavoritesScreen(onHome = { navController.navigateTo(FoodScanDestination.Scanner) },
                    products = libraryState.favorites, onProduct = openProduct,
                    onRemove = library::removeFavorite, busy = busy)
            }
        }
        composable(FoodScanDestination.Profile.route) { entry ->
            CurrentDestination(navController, entry) {
                ProfileScreen(scenario = libraryState.scenario, historyEnabled = libraryState.historyEnabled, busy = busy,
                    onScenario = library::selectScenario, onHistoryEnabled = library::setHistoryEnabled,
                    onHistory = { navController.navigate("history") },
                    onAdditives = { navController.navigate("additives") },
                    onMethodology = { navController.navigate("methodology") }, onDeleteAll = library::clearAll)
            }
        }
        composable(
            route = ProductDestination.route,
            arguments = listOf(navArgument(ProductDestination.argument) { type = NavType.StringType }),
        ) { entry ->
            CurrentDestination(navController, entry) {
                ProductRoute(
                    services = services,
                    onAdditive = openAdditive,
                    onMethodology = { navController.navigate("methodology") },
                    onBack = {
                        if (!navController.popBackStack()) navController.navigateTo(FoodScanDestination.Home)
                    },
                )
            }
        }
        composable("history") { entry ->
            CurrentDestination(navController, entry) {
                HistoryScreen(libraryState.history, { navController.popBackStack() }, openProduct,
                    library::deleteHistory, library::clearHistory, busy)
            }
        }
        composable("additives") { entry ->
            CurrentDestination(navController, entry) { AdditiveScreen(null, { navController.popBackStack() }, openAdditive) }
        }
        composable("additive/{code}", arguments = listOf(navArgument("code") { type = NavType.StringType })) { entry ->
            CurrentDestination(navController, entry) {
                AdditiveScreen(entry.arguments?.getString("code"), { navController.popBackStack() }, openAdditive)
            }
        }
        composable("methodology") { entry ->
            CurrentDestination(navController, entry) { MethodologyScreen { navController.popBackStack() } }
        }
    }
}

/** NavHost may retain outgoing entries while settling rapid navigation changes.
 * Keep their saveable state, but never paint or expose an outgoing screen.
 */
@Composable
private fun CurrentDestination(
    navController: NavHostController,
    entry: NavBackStackEntry,
    content: @Composable () -> Unit,
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val semantics = if (currentEntry?.id == entry.id) Modifier else Modifier.clearAndSetSemantics {}
    Box(
        Modifier.fillMaxSize().then(semantics).drawWithContent {
            if (currentEntry?.id == entry.id) drawContent()
        },
    ) {
        content()
    }
}
