package com.menuplayer

import android.app.Application
import android.util.Log
import com.menuplayer.utils.CacheManager
import com.menuplayer.utils.PrefsManager

class MenuPlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "MenuPlayerApp started")

        // Initialize preferences
        PrefsManager.init(this)
        CacheManager.init(this)
    }

    companion object {
        private const val TAG = "MenuPlayerApp"
        lateinit var instance: MenuPlayerApp
            private set
    }
}
