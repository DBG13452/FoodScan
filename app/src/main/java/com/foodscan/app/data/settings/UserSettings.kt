package com.foodscan.app.data.settings

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.foodscan.app.domain.model.NutritionScenario
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.foodScanSettings by preferencesDataStore(
    name = "foodscan-settings",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

class UserSettings(context: Context, testStore: DataStore<Preferences>? = null) {
    private val store = testStore ?: context.foodScanSettings
    private val scenarioKey = stringPreferencesKey("scenario")
    private val historyKey = booleanPreferencesKey("history_enabled")
    private val preferences = store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }
    val scenario = preferences.map { values ->
        runCatching { NutritionScenario.valueOf(values[scenarioKey] ?: "NORMAL") }.getOrDefault(NutritionScenario.NORMAL)
    }
    val historyEnabled = preferences.map { it[historyKey] ?: true }
    suspend fun setScenario(value: NutritionScenario) { store.edit { it[scenarioKey] = value.name } }
    suspend fun setHistoryEnabled(value: Boolean) { store.edit { it[historyKey] = value } }
    suspend fun reset() { store.edit { it.clear() } }
}
