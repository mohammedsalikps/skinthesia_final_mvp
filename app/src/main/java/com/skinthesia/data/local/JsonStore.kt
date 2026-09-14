package com.skinthesia.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** One JSON configuration for everything Skinthesia persists as text. */
val SkinthesiaJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

private val Context.skinthesiaPreferences: DataStore<Preferences> by preferencesDataStore(name = "skinthesia_prefs")

fun Context.skinthesiaDataStore(): DataStore<Preferences> = applicationContext.skinthesiaPreferences

/**
 * Stores one serializable value as JSON under a DataStore key. Unknown or corrupt
 * data (for example from a future schema) falls back to [default] rather than crashing.
 */
class JsonPreferenceStore<T>(
    private val dataStore: DataStore<Preferences>,
    name: String,
    private val serializer: KSerializer<T>,
    private val default: () -> T,
    private val json: Json = SkinthesiaJson,
) {
    private val key = stringPreferencesKey(name)

    val data: Flow<T> = dataStore.data.map { prefs -> decode(prefs[key]) }

    suspend fun current(): T = data.first()

    suspend fun update(transform: (T) -> T): T {
        var result: T? = null
        dataStore.edit { prefs ->
            val next = transform(decode(prefs[key]))
            prefs[key] = json.encodeToString(serializer, next)
            result = next
        }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    suspend fun clear() {
        dataStore.edit { it.remove(key) }
    }

    private fun decode(raw: String?): T =
        raw?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() } ?: default()
}
