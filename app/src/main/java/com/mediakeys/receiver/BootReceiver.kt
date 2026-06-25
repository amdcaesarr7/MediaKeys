package com.mediakeys.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mediakeys.data.PreferencesManager
import com.mediakeys.service.MediaForegroundService

/**
 * Starts the MediaKeys foreground service on device boot.
 *
 * Only fires if the user enabled "Start on Boot" in Settings and
 * the service toggle is currently on.
 * The AccessibilityService itself is managed by Android settings — we just
 * ensure the foreground service companion is alive so notifications show.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val prefs = PreferencesManager.getInstance(context)
        if (prefs.startOnBoot && prefs.serviceEnabled) {
            val serviceIntent = Intent(context, MediaForegroundService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
