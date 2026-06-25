package com.mediakeys.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import com.mediakeys.data.PreferencesManager
import com.mediakeys.util.Logger

/**
 * Orchestrates media control with robust Fallback Launchers and precise session targeting.
 */
class MediaSessionHelper(
    private val context: Context,
    private val prefs: PreferencesManager
) {

    private val sessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())

    fun getActiveMediaApp(): String? = findActiveController()?.packageName

    fun dispatch(action: String) {
        Logger.i("Executing Action: $action")
        
        // Special package launch
        if (action.contains('.')) {
            launchWithFeedback(action)
            return
        }

        val controller = findActiveController()
        
        if (controller == null) {
            Logger.d("No session. Fallback: Dispatch global key.")
            // Use KEYCODE_MEDIA_PLAY_PAUSE instead of specific play/pause for fallback
            val keyCode = if (action == PreferencesManager.ACTION_PLAY_PAUSE) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            } else {
                actionToKeyCode(action)
            }
            sendGlobalKey(keyCode)
            
            // If it's a play/pause action and nothing is playing, force launch
            if (action == PreferencesManager.ACTION_PLAY_PAUSE && !audioManager.isMusicActive) {
                Logger.i("Music inactive. Triggering Fallback Launcher.")
                launchWithFeedback()
            }
            return
        }

        Logger.d("Targeting session: ${controller.packageName} (State: ${controller.playbackState?.state})")
        val controls = controller.transportControls ?: run {
            Logger.e("Transport error for ${controller.packageName}")
            return
        }
        
        when (action) {
            PreferencesManager.ACTION_NEXT -> controls.skipToNext()
            PreferencesManager.ACTION_PREV -> controls.skipToPrevious()
            PreferencesManager.ACTION_PLAY_PAUSE -> {
                // IMPORTANT: Use KEYCODE_MEDIA_PLAY_PAUSE via the controller to let the APP decide
                // if it should resume the current track or pause. Specific .play() commands
                // can sometimes trigger a "recommended" or new song in apps like YT Music.
                val eventTime = SystemClock.uptimeMillis()
                controller.dispatchMediaButtonEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0))
                controller.dispatchMediaButtonEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0))
            }
        }
    }

    private fun findActiveController(): MediaController? {
        val listenerComponent = ComponentName(context, "com.mediakeys.service.MediaNotificationListenerService")
        val sessions = try {
            sessionManager.getActiveSessions(listenerComponent)
        } catch (e: SecurityException) {
            Logger.e("SecurityException getting sessions: ${e.message}")
            emptyList()
        }

        if (sessions.isEmpty()) return null

        // Priority 1: The session that is actually PLAYING right now
        val currentlyPlaying = sessions.find { it.playbackState?.state == PlaybackState.STATE_PLAYING }
        if (currentlyPlaying != null) return currentlyPlaying

        // Priority 2: The user's preferred app session (even if paused)
        val preferred = prefs.preferredMediaApp
        val preferredSession = sessions.find { it.packageName == preferred }
        if (preferredSession != null) return preferredSession

        // Priority 3: The most recently active session
        return sessions.firstOrNull()
    }

    private fun launchWithFeedback(packageName: String = prefs.preferredMediaApp) {
        Logger.i("Launching Package: $packageName")
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: run {
            Logger.e("Launch Failed: $packageName not installed")
            
            // Intelligent Fallback Chain
            when (packageName) {
                PreferencesManager.APP_RVX_MUSIC -> {
                    Logger.d("RVX not found. Retrying with ReVanced fallback...")
                    launchWithFeedback(PreferencesManager.APP_REVANCED_MUSIC)
                }
                PreferencesManager.APP_REVANCED_MUSIC -> {
                    Logger.d("ReVanced not found. Retrying with standard YT Music fallback...")
                    launchWithFeedback(PreferencesManager.APP_YOUTUBE_MUSIC)
                }
                else -> {
                    // Stop fallback chain for Spotify or other apps
                }
            }
            return
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        // Force a Play command after app warms up
        mainHandler.postDelayed({ 
            Logger.d("Warm-up complete. Sending Global PLAY_PAUSE.")
            sendGlobalKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }, 2000)
    }

    private fun sendGlobalKey(keyCode: Int) {
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) return
        val now = SystemClock.uptimeMillis()
        audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
        audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
    }

    private fun actionToKeyCode(action: String): Int = when (action) {
        PreferencesManager.ACTION_NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
        PreferencesManager.ACTION_PREV -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
        PreferencesManager.ACTION_PLAY_PAUSE -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        else -> KeyEvent.KEYCODE_UNKNOWN
    }

    fun destroy() {
        mainHandler.removeCallbacksAndMessages(null)
    }
}
