package com.menuplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.menuplayer.utils.PrefsManager

/**
 * Entry point. Shows logo for 1.5 s then routes to:
 *  • SettingsActivity  – if server URL has never been configured
 *  • PlayerActivity    – otherwise
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val next = if (PrefsManager.isConfigured) {
                Intent(this, PlayerActivity::class.java)
            } else {
                Intent(this, SettingsActivity::class.java)
            }
            startActivity(next)
            finish()
        }, 1500)
    }
}
