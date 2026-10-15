package com.foodscan.app

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.foodscan.app.data.AppContainer
import com.foodscan.app.data.local.FoodScanDatabase
import com.foodscan.app.data.settings.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.rules.ExternalResource
import java.io.File
import java.util.UUID

/** Isolates tests from the user's actual favorites, history and settings. */
class TestEnvironment : ExternalResource() {
    lateinit var services: AppContainer
    private lateinit var database: FoodScanDatabase
    private lateinit var scope: CoroutineScope
    override fun before() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FoodScanDatabase::class.java).build()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(context.cacheDir, "test-${UUID.randomUUID()}.preferences_pb")
        val store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
        services = AppContainer(context, database, UserSettings(context, store))
    }
    override fun after() { scope.cancel(); database.close() }
}
