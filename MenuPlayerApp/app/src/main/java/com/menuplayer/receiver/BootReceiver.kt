package com.menuplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.menuplayer.PlayerActivity

/**
 * Automatically launches PlayerActivity when the device boots.
 * Registered for BOOT_COMPLETED and QUICKBOOT_POWERON (Huawei/some Android TV).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Boot received ($action) – launching PlayerActivity")
            val launch = Intent(context, PlayerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(launch)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
