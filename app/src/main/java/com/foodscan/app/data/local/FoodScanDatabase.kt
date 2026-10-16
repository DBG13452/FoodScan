package com.foodscan.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "products")
data class StoredProduct(@PrimaryKey val id: String, val payload: String)

@Entity(tableName = "favorites", foreignKeys = [ForeignKey(
    entity = StoredProduct::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.CASCADE,
)])
data class FavoriteEntry(@PrimaryKey val productId: String, val savedAt: Long)

@Entity(tableName = "history", indices = [Index(value = ["productId"], unique = true), Index("viewedAt")], foreignKeys = [ForeignKey(
    entity = StoredProduct::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.CASCADE,
)])
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0,
    val productId: String,
    val viewedAt: Long,
)

data class HistoryRow(val eventId: Long, val payload: String, val viewedAt: Long)

@Dao
abstract class LibraryDao {
    @Upsert abstract suspend fun saveProduct(product: StoredProduct)
    @Insert(onConflict = OnConflictStrategy.IGNORE) abstract suspend fun addFavorite(entry: FavoriteEntry)
    @Insert abstract suspend fun addHistory(entry: HistoryEntry)
    @Query("UPDATE history SET viewedAt = :viewedAt WHERE productId = :productId")
    abstract suspend fun updateHistory(productId: String, viewedAt: Long): Int
    @Query("SELECT * FROM products WHERE id = :id") abstract suspend fun product(id: String): StoredProduct?
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE productId = :id)") abstract fun isFavorite(id: String): Flow<Boolean>
    @Query("SELECT products.* FROM products JOIN favorites ON products.id = favorites.productId ORDER BY savedAt DESC, products.id")
    abstract fun favorites(): Flow<List<StoredProduct>>
    @Query("SELECT history.eventId, products.payload, history.viewedAt FROM history JOIN products ON products.id = history.productId ORDER BY viewedAt DESC, eventId DESC")
    abstract fun history(): Flow<List<HistoryRow>>
    @Query("DELETE FROM favorites WHERE productId = :id") abstract suspend fun removeFavorite(id: String)
    @Query("DELETE FROM history WHERE eventId = :eventId") abstract suspend fun removeHistory(eventId: Long)
    @Query("DELETE FROM history") abstract suspend fun clearHistoryRows()
    @Query("DELETE FROM favorites") abstract suspend fun clearFavoriteRows()
    @Query("DELETE FROM products WHERE id NOT IN (SELECT productId FROM favorites) AND id NOT IN (SELECT productId FROM history)")
    abstract suspend fun removeOrphans()
    @Query("DELETE FROM products") abstract suspend fun clearProducts()

    @Transaction open suspend fun recordView(product: StoredProduct, viewedAt: Long, recordHistory: Boolean) {
        saveProduct(product)
        if (recordHistory && updateHistory(product.id, viewedAt) == 0) {
            addHistory(HistoryEntry(productId = product.id, viewedAt = viewedAt))
        }
        removeOrphans()
    }
    @Transaction open suspend fun setFavorite(product: StoredProduct, selected: Boolean, time: Long) {
        saveProduct(product)
        if (selected) addFavorite(FavoriteEntry(product.id, time)) else removeFavorite(product.id)
        removeOrphans()
    }
    @Transaction open suspend fun deleteHistory(eventId: Long) { removeHistory(eventId); removeOrphans() }
    @Transaction open suspend fun clearHistory() { clearHistoryRows(); removeOrphans() }
    @Transaction open suspend fun clearAll() { clearHistoryRows(); clearFavoriteRows(); clearProducts() }
}

@Database(entities = [StoredProduct::class, FavoriteEntry::class, HistoryEntry::class], version = 2, exportSchema = true)
abstract class FoodScanDatabase : RoomDatabase() {
    abstract fun library(): LibraryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Keep the most recent scan, with a deterministic winner for equal timestamps.
                // Product snapshots and favorites are unaffected.
                db.execSQL("""DELETE FROM history WHERE EXISTS (
                    SELECT 1 FROM history AS newer WHERE newer.productId = history.productId
                    AND (newer.viewedAt > history.viewedAt OR
                        (newer.viewedAt = history.viewedAt AND newer.eventId > history.eventId)))""")
                db.execSQL("DROP INDEX IF EXISTS index_history_productId")
                db.execSQL("CREATE UNIQUE INDEX index_history_productId ON history (productId)")
            }
        }
    }
}
