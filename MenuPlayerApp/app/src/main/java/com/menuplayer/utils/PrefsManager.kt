package com.menuplayer.utils

import android.content.Context
import android.content.SharedPreferences
import com.menuplayer.BuildConfig

/**
 * Centralized preference store for device-specific settings.
 * All values survive app restarts.
 */
object PrefsManager {

    private const val PREFS_NAME = "menuplayer_prefs"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── Server connection ───────────────────────────────────────────────────

    var serverUrl: String
        get() = prefs.getString("server_url", BuildConfig.BASE_URL) ?: BuildConfig.BASE_URL
        set(v) = prefs.edit().putString("server_url", v).apply()

    var apiToken: String
        get() = prefs.getString("api_token", "") ?: ""
        set(v) = prefs.edit().putString("api_token", v).apply()

    var screenId: String
        get() = prefs.getString("screen_id", BuildConfig.DEFAULT_SCREEN_ID) ?: BuildConfig.DEFAULT_SCREEN_ID
        set(v) = prefs.edit().putString("screen_id", v).apply()

    // Mark as configured on first install so it goes straight to player
    var isConfigured: Boolean
        get() = prefs.getBoolean("is_configured", true)   // default true → skip setup screen
        set(v) = prefs.edit().putBoolean("is_configured", v).apply()

    // ── Settings lock PIN ───────────────────────────────────────────────────

    var settingsPin: String
        get() = prefs.getString("settings_pin", "1234") ?: "1234"
        set(v) = prefs.edit().putString("settings_pin", v).apply()

    // ── Refresh interval ────────────────────────────────────────────────────

    var refreshIntervalSec: Int
        get() = prefs.getInt("refresh_interval", 30)
        set(v) = prefs.edit().putInt("refresh_interval", v).apply()
}
