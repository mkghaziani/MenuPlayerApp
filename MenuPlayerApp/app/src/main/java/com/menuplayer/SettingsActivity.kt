package com.menuplayer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.menuplayer.api.RetrofitClient
import com.menuplayer.databinding.ActivitySettingsBinding
import com.menuplayer.utils.PrefsManager

/**
 * One-time (or PIN-protected) configuration screen.
 * Lets staff set:
 *  - ERPNext server URL
 *  - API token (key:secret)
 *  - Screen ID
 *  - Settings PIN
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-fill saved values
        binding.etServerUrl.setText(PrefsManager.serverUrl)
        binding.etApiToken.setText(PrefsManager.apiToken)
        binding.etScreenId.setText(PrefsManager.screenId)
        binding.etPin.setText(PrefsManager.settingsPin)

        binding.btnSave.setOnClickListener { saveAndLaunch() }
    }

    private fun saveAndLaunch() {
        val url    = binding.etServerUrl.text.toString().trim()
        val token  = binding.etApiToken.text.toString().trim()
        val screen = binding.etScreenId.text.toString().trim()
        val pin    = binding.etPin.text.toString().trim()

        if (url.isBlank() || screen.isBlank()) {
            Toast.makeText(this, "Server URL and Screen ID are required", Toast.LENGTH_SHORT).show()
            return
        }

        PrefsManager.serverUrl  = url
        PrefsManager.apiToken   = token
        PrefsManager.screenId   = screen
        PrefsManager.settingsPin = pin.ifBlank { "1234" }
        PrefsManager.isConfigured = true

        // Rebuild retrofit with new URL
        RetrofitClient.invalidate()

        Toast.makeText(this, "Saved! Starting player…", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, PlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
