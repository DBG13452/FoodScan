package com.foodscan.app.navigation

import android.net.Uri

object ProductDestination {
    const val argument = "productId"
    const val route = "product/{$argument}"

    fun routeFor(id: String): String = "product/${Uri.encode(id)}"
}
