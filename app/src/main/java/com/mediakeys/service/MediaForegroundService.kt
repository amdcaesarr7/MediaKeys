package com.mediakeys.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.mediakeys.MainActivity
import com.mediakeys.R
import com.mediakeys.util.Logger

/**
 * Foreground Service that keeps MediaKeys alive and hosts a [MediaSessionCompat].
 *
 * Hosting a MediaSession ensures that the app is integrated into the Android
 * media stack, making it a "proper" player in the eyes of the OS.
 */
class MediaForegroundService : LifecycleService() {

    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()
        Logger.i("MediaForegroundService Creating")
        
        setupMediaSession()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "MediaKeysSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    Logger.d("MediaSession: onPlay received")
                }
                override fun onPause() {
                    Logger.d("MediaSession: onPause received")
                }
            })

            isActive = true
            
            val state = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
                .build()
            setPlaybackState(state)
        }
        Logger.d("MediaSessionCompat initialized and active")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i("MediaForegroundService Destroying")
        mediaSession?.apply {
            isActive = false
            release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        mediaSession?.let {
            builder.setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(it.sessionToken)
                .setShowActionsInCompactView(0))
        }

        return builder.build()
    }

    companion object {
        const val CHANNEL_ID = "mediakeys_service"
        const val NOTIFICATION_ID = 1001
    }
}
