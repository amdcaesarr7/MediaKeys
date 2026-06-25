package com.mediakeys.service

import android.service.notification.NotificationListenerService
import android.util.Log

/**
 * Minimal NotificationListenerService required to grant the app
 * permission to access active MediaSessions.
 * 
 * Without this service (and its associated permission), the app cannot
 * reliably control third-party media players in the background on modern Android.
 */
class MediaNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("MediaKeys", "MediaNotificationListenerService connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("MediaKeys", "MediaNotificationListenerService disconnected")
    }
}
