package com.foodscan.app.storage

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foodscan.app.data.local.FoodScanDatabase
import com.foodscan.app.data.local.UserLibrary
import com.foodscan.app.data.product.DemoProductRepository
import com.foodscan.app.data.settings.UserSettings
import com.foodscan.app.domain.model.NutritionScenario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class StoragePersistenceTest {
    @Test fun databaseAndScenarioSurviveReopenAndDeletionKeepsFavorites() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "test-${UUID.randomUUID()}.db"
        val preferencesFile = File(context.cacheDir, "test-${UUID.randomUUID()}.preferences_pb")
        fun database() = Room.databaseBuilder(context, FoodScanDatabase::class.java, databaseName).build()
        var db = database()
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        fun settings() = UserSettings(context, PreferenceDataStoreFactory.create(scope = scope, produceFile = { preferencesFile }))
        var preferences = settings()
        val product = DemoProductRepository.products.first()
        try {
            var library = UserLibrary(db.library())
            library.recordView(product, true)
            library.setFavorite(product, true)
            library.setFavorite(product, true)
            assertEquals(1, library.favorites.first().size)
            preferences.setScenario(NutritionScenario.SPORT)
            preferences.setHistoryEnabled(false)
            db.close()
            scope.coroutineContext[Job]!!.cancelAndJoin()
            db = database()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            preferences = settings()
            library = UserLibrary(db.library())
            assertEquals(product, library.favorites.first().single())
            assertEquals(1, library.history.first().size)
            assertEquals(NutritionScenario.SPORT, preferences.scenario.first())
            assertFalse(preferences.historyEnabled.first())
            library.clearHistory()
            assertTrue(library.history.first().isEmpty())
            assertEquals(product, library.find(product.id))
            assertEquals(1, library.favorites.first().size)
            library.setFavorite(product, false)
            assertNull(library.find(product.id))
            library.recordView(product, false)
            assertTrue(library.history.first().isEmpty())
            assertNull(library.find(product.id))
            library.recordView(product, true)
            library.recordView(product, true)
            val first = library.history.first().first()
            library.deleteHistory(first.eventId)
            assertEquals(1, library.history.first().size)
            library.clearAll()
            preferences.reset()
            assertTrue(library.history.first().isEmpty())
            assertTrue(library.favorites.first().isEmpty())
            assertEquals(NutritionScenario.NORMAL, preferences.scenario.first())
        } finally {
            db.close()
            scope.coroutineContext[Job]!!.cancelAndJoin()
            context.deleteDatabase(databaseName)
            preferencesFile.delete()
        }
    }
}
