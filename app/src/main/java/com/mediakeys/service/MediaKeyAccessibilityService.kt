package com.mediakeys.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.mediakeys.data.PreferencesManager
import com.mediakeys.gesture.GestureEngine
import com.mediakeys.media.MediaSessionHelper
import com.mediakeys.util.Logger

/**
 * Core AccessibilityService — handles button streams and global actions.
 */
class MediaKeyAccessibilityService : AccessibilityService(), GestureEngine.GestureListener {

    private lateinit var prefs: PreferencesManager
    private lateinit var gestureEngine: GestureEngine
    private lateinit var mediaHelper: MediaSessionHelper

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PreferencesManager.getInstance(this)
        gestureEngine = GestureEngine(prefs, this)
        mediaHelper = MediaSessionHelper(this, prefs)

        Logger.i("[System] Accessibility Service Active")
        broadcastServiceState(running = true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i("[System] Service Shutting Down")
        gestureEngine.destroy()
        mediaHelper.destroy()
        broadcastServiceState(running = false)
    }

    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!prefs.serviceEnabled) return false
        return gestureEngine.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onGesture(action: String) {
        if (action == PreferencesManager.ACTION_HOME) {
            val success = performGlobalAction(GLOBAL_ACTION_HOME)
            Logger.i("Executing Action: HOME SCREEN (Success: $success)")
        } else {
            mediaHelper.dispatch(action)
        }
    }

    private fun broadcastServiceState(running: Boolean) {
        val intent = Intent(ACTION_SERVICE_STATE_CHANGED).apply {
            putExtra(EXTRA_RUNNING, running)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    companion object {
        const val ACTION_SERVICE_STATE_CHANGED = "com.mediakeys.ACTION_SERVICE_STATE_CHANGED"
        const val EXTRA_RUNNING = "running"
    }
}
