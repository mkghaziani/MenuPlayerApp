package com.menuplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.menuplayer.api.RetrofitClient
import com.menuplayer.databinding.ActivityPlayerBinding
import com.menuplayer.model.MenuItem
import com.menuplayer.model.MenuResponse
import com.menuplayer.utils.CacheManager
import com.menuplayer.utils.NetworkUtils
import com.menuplayer.utils.PrefsManager
import com.menuplayer.utils.SyncForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private var exoPlayer: ExoPlayer? = null
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val slideHandler = Handler(Looper.getMainLooper())

    private var menuItems: List<MenuItem> = emptyList()
    private var currentIndex = 0

    // PIN entry buffer for hidden settings access
    private val keyBuffer = StringBuilder()
    private val keyBufferHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen kiosk setup
        setupKioskMode()

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Start foreground service to keep process alive
        startService(Intent(this, SyncForegroundService::class.java))

        // Show cached data immediately, then refresh
        loadCachedData()
        fetchMenu()
        schedulePeriodicRefresh()
    }

    // ── Kiosk ───────────────────────────────────────────────────────────────

    private fun setupKioskMode() {
        // Fullscreen + hide nav bar
        val flags = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = flags
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) setupKioskMode()
    }

    // ── Data loading ─────────────────────────────────────────────────────────

    private fun loadCachedData() {
        val cached = CacheManager.load()
        if (cached != null) applyMenuResponse(cached)
    }

    private fun fetchMenu() {
        lifecycleScope.launch {
            try {
                val online = NetworkUtils.isOnline(this@PlayerActivity)
                if (!online) {
                    Log.w(TAG, "Offline – using cache")
                    showOfflineIndicator()
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getScreenData(PrefsManager.screenId)
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        CacheManager.save(body)
                        applyMenuResponse(body)
                        hideOfflineIndicator()
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fetch failed: ${e.message}")
                showOfflineIndicator()
            }
        }
    }

    private fun applyMenuResponse(response: MenuResponse) {
        menuItems = response.items.filter { it.available }
        currentIndex = 0

        // Update refresh interval from server settings
        PrefsManager.refreshIntervalSec = response.settings.refreshIntervalSec

        if (menuItems.isNotEmpty()) {
            slideHandler.removeCallbacksAndMessages(null)
            showItem(menuItems[currentIndex])
        } else {
            showEmptyState()
        }
    }

    // ── Slideshow ────────────────────────────────────────────────────────────

    private fun showItem(item: MenuItem) {
        binding.tvItemName.text = item.name
        binding.tvItemDescription.text = item.description
        binding.tvItemPrice.text = if (item.price > 0) "Rs ${item.price.toInt()}" else ""

        if (item.hasVideo) {
            showVideo(item.video)
        } else if (item.hasImage) {
            showImage(item.image, item.displayDurationSec)
        } else {
            advanceSlide(item.displayDurationSec)
        }
    }

    private fun showImage(url: String, durationSec: Int) {
        stopVideo()
        binding.videoView.visibility = View.GONE
        binding.imageView.visibility = View.VISIBLE

        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.bg_loading)
            .error(R.drawable.bg_error)
            .into(binding.imageView)

        scheduleNextSlide(durationSec * 1000L)
    }

    private fun showVideo(url: String) {
        binding.imageView.visibility = View.GONE
        binding.videoView.visibility = View.VISIBLE

        stopVideo()

        exoPlayer = ExoPlayer.Builder(this).build().also { player ->
            binding.videoView.player = player
            player.setMediaItem(MediaItem.fromUri(url))
            player.repeatMode = Player.REPEAT_MODE_OFF
            player.prepare()
            player.play()

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        advanceSlide(0)
                    }
                }
            })
        }
    }

    private fun stopVideo() {
        exoPlayer?.let {
            it.stop()
            it.release()
        }
        exoPlayer = null
    }

    private fun scheduleNextSlide(delayMs: Long) {
        slideHandler.removeCallbacksAndMessages(null)
        slideHandler.postDelayed({ advanceSlide(0) }, delayMs)
    }

    private fun advanceSlide(extraDelay: Int) {
        if (menuItems.isEmpty()) return
        currentIndex = (currentIndex + 1) % menuItems.size
        val delay = if (extraDelay > 0) extraDelay * 1000L else 0L
        slideHandler.postDelayed({ showItem(menuItems[currentIndex]) }, delay)
    }

    // ── Periodic refresh ─────────────────────────────────────────────────────

    private fun schedulePeriodicRefresh() {
        val interval = PrefsManager.refreshIntervalSec * 1000L
        refreshHandler.postDelayed({
            fetchMenu()
            schedulePeriodicRefresh()
        }, interval)
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun showEmptyState() {
        binding.tvItemName.text = getString(R.string.no_items)
        binding.tvItemDescription.text = ""
        binding.tvItemPrice.text = ""
        binding.imageView.setImageResource(R.drawable.bg_loading)
        binding.imageView.visibility = View.VISIBLE
        binding.videoView.visibility = View.GONE
    }

    private fun showOfflineIndicator() {
        binding.tvOffline.visibility = View.VISIBLE
    }

    private fun hideOfflineIndicator() {
        binding.tvOffline.visibility = View.GONE
    }

    // ── Hidden settings access via remote key sequence ────────────────────────
    // Press: UP UP DOWN DOWN ENTER  →  prompts for PIN

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        keyBuffer.append(keyCode)
        keyBufferHandler.removeCallbacksAndMessages(null)
        keyBufferHandler.postDelayed({ keyBuffer.clear() }, 3000)

        if (keyBuffer.endsWith(SECRET_SEQUENCE)) {
            keyBuffer.clear()
            promptForPin()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun promptForPin() {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter PIN"
        }
        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setView(input)
            .setPositiveButton("Open") { _, _ ->
                if (input.text.toString() == PrefsManager.settingsPin) {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        exoPlayer?.play()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshHandler.removeCallbacksAndMessages(null)
        slideHandler.removeCallbacksAndMessages(null)
        keyBufferHandler.removeCallbacksAndMessages(null)
        stopVideo()
    }

    companion object {
        private const val TAG = "PlayerActivity"

        // KeyEvent codes: UP=19, DOWN=20, ENTER=66  →  "19192020666" as string
        private const val SECRET_SEQUENCE = "${KeyEvent.KEYCODE_DPAD_UP}" +
                "${KeyEvent.KEYCODE_DPAD_UP}" +
                "${KeyEvent.KEYCODE_DPAD_DOWN}" +
                "${KeyEvent.KEYCODE_DPAD_DOWN}" +
                "${KeyEvent.KEYCODE_ENTER}"
    }
}
