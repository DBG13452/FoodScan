package com.foodscan.app.data

import android.content.Context
import androidx.room.Room
import com.foodscan.app.data.local.FoodScanDatabase
import com.foodscan.app.data.local.UserLibrary
import com.foodscan.app.data.settings.UserSettings
import com.foodscan.app.data.product.ProductServices
import coil.imageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppContainer(context: Context, database: FoodScanDatabase? = null, settings: UserSettings? = null) {
    private val appContext = context.applicationContext
    private val database = database ?: Room.databaseBuilder(appContext, FoodScanDatabase::class.java, "foodscan.db").build()
    val library = UserLibrary(this.database.library())
    val settings = settings ?: UserSettings(appContext)
    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    suspend fun clearUserData() {
        library.clearAll()
        settings.reset()
        ProductServices.clearCache()
        withContext(Dispatchers.IO) {
            appContext.imageLoader.memoryCache?.clear()
            appContext.imageLoader.diskCache?.clear()
        }
    }
}

object AppServices {
    @Volatile private var instance: AppContainer? = null
    fun get(context: Context): AppContainer = instance ?: synchronized(this) {
        instance ?: AppContainer(context).also { instance = it }
    }
}
