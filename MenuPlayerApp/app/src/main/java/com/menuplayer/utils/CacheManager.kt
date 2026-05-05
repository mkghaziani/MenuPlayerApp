package com.menuplayer.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.menuplayer.model.MenuResponse

/**
 * Saves the last successfully fetched MenuResponse to SharedPreferences.
 * Used as offline fallback when the network is unavailable.
 */
object CacheManager {

    private const val TAG = "CacheManager"
    private const val PREFS_NAME = "menu_cache"
    private const val KEY_DATA = "data"
    private const val KEY_TIMESTAMP = "timestamp"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun save(response: MenuResponse) {
        try {
            prefs.edit()
                .putString(KEY_DATA, gson.toJson(response))
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .apply()
            Log.d(TAG, "Cache saved – ${response.items.size} items")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cache", e)
        }
    }

    fun load(): MenuResponse? {
        return try {
            val json = prefs.getString(KEY_DATA, null) ?: return null
            gson.fromJson(json, MenuResponse::class.java).also {
                Log.d(TAG, "Cache loaded – ${it.items.size} items (age ${cacheAgeMinutes()} min)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load cache", e)
            null
        }
    }

    fun hasCache(): Boolean = prefs.contains(KEY_DATA)

    /** Age of the cached data in minutes */
    fun cacheAgeMinutes(): Long {
        val ts = prefs.getLong(KEY_TIMESTAMP, 0L)
        return if (ts == 0L) Long.MAX_VALUE
        else (System.currentTimeMillis() - ts) / 60_000
    }

    fun clear() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Cache cleared")
    }
}
