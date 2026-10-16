package com.foodscan.app.storage

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.foodscan.app.data.local.FoodScanDatabase
import com.foodscan.app.data.local.ProductSnapshot
import com.foodscan.app.data.local.StoredProduct
import com.foodscan.app.data.product.DemoProductRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class HistoryUniquenessTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val products = DemoProductRepository.products.take(2).map { StoredProduct(it.id, ProductSnapshot.encode(it)) }

    @Test fun reopeningUpdatesTheSameEntryAndMovesItAboveOtherProducts() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, FoodScanDatabase::class.java).build()
        try {
            val dao = db.library()
            dao.recordView(products[0], 100, true)
            val originalId = dao.history().first().single().eventId
            dao.recordView(products[1], 200, true)
            val updated = products[0].copy(payload = ProductSnapshot.encode(
                DemoProductRepository.products[0].copy(name = "Updated product")))
            dao.recordView(updated, 300, true)
            val history = dao.history().first()
            assertEquals(2, history.size)
            assertEquals(originalId, history.first().eventId)
            assertEquals(300L, history.first().viewedAt)
            assertEquals(updated.payload, history.first().payload)
            dao.recordView(updated, 400, false)
            assertEquals(300L, dao.history().first().first().viewedAt)
            dao.deleteHistory(originalId)
            assertEquals(listOf(products[1].payload), dao.history().first().map { it.payload })
        } finally { db.close() }
    }

    @Test fun simultaneousViewsCannotCreateDuplicateEntries() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, FoodScanDatabase::class.java).build()
        try {
            coroutineScope {
                repeat(20) { launch { db.library().recordView(products[0], 100, true) } }
            }
            assertEquals(1, db.library().history().first().size)
        } finally { db.close() }
    }

    @Test fun migrationKeepsLatestScanAndFavoritesAndSurvivesReopening() = runBlocking {
        val name = "history-migration-${UUID.randomUUID()}.db"
        val file = context.getDatabasePath(name)
        file.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            old.execSQL("CREATE TABLE products (id TEXT NOT NULL PRIMARY KEY, payload TEXT NOT NULL)")
            old.execSQL("""CREATE TABLE favorites (productId TEXT NOT NULL PRIMARY KEY, savedAt INTEGER NOT NULL,
                FOREIGN KEY(productId) REFERENCES products(id) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            old.execSQL("""CREATE TABLE history (eventId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productId TEXT NOT NULL, viewedAt INTEGER NOT NULL,
                FOREIGN KEY(productId) REFERENCES products(id) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            old.execSQL("CREATE INDEX index_history_productId ON history(productId)")
            old.execSQL("CREATE INDEX index_history_viewedAt ON history(viewedAt)")
            products.forEach { old.execSQL("INSERT INTO products VALUES (?, ?)", arrayOf(it.id, it.payload)) }
            old.execSQL("INSERT INTO favorites VALUES (?, 10)", arrayOf(products[0].id))
            // The newest timestamp deliberately does not have the highest event id.
            listOf(1 to 300, 2 to 100, 3 to 300, 4 to 200).forEach { (id, time) ->
                old.execSQL("INSERT INTO history VALUES (?, ?, ?)", arrayOf<Any>(id, products[0].id, time))
            }
            old.execSQL("INSERT INTO history VALUES (5, ?, 250)", arrayOf(products[1].id))
            old.version = 1
        }
        fun open() = Room.databaseBuilder(context, FoodScanDatabase::class.java, name)
            .addMigrations(FoodScanDatabase.MIGRATION_1_2).build()
        var db = open()
        try {
            val rows = db.library().history().first()
            assertEquals(2, rows.size)
            assertEquals(3L, rows.first().eventId)
            assertEquals(300L, rows.first().viewedAt)
            assertEquals(products[0], db.library().favorites().first().single())
            assertEquals(products[1], db.library().product(products[1].id))
            db.library().recordView(products[0], 500, true)
            db.close()
            db = open()
            assertEquals(2, db.library().history().first().size)
            assertEquals(500L, db.library().history().first().first().viewedAt)
            assertEquals(3L, db.library().history().first().first().eventId)
            db.library().deleteHistory(3L)
            assertEquals(1, db.library().history().first().size)
            assertEquals(products[0], db.library().favorites().first().single())
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}
