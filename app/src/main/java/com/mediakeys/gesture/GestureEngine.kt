package com.mediakeys.gesture

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.mediakeys.data.PreferencesManager
import com.mediakeys.util.Logger

/**
 * Optimized Gesture Engine.
 * 
 * Fixes identified from logs:
 * 1. Immediate Chord Execution: Chords now fire on the first down event and lock out sequences.
 * 2. Sequence Mapping: Added explicit handling for [DOWN, UP] and [UP, DOWN] to prevent "No Action" logs.
 * 3. Buffer Hygiene: Stricter clearing of buffers to prevent "phantom" key additions.
 */
class GestureEngine(
    private val prefs: PreferencesManager,
    private val listener: GestureListener
) {

    interface GestureListener {
        fun onGesture(action: String)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val sequenceBuffer = mutableListOf<Int>()
    private var processRunnable: Runnable? = null
    
    private var lastDownTime = 0L
    private val DEBOUNCE_MS = 50L 

    private var upPressed = false
    private var downPressed = false
    private var powerPressed = false
    
    // Lock to prevent sequence processing if a chord was just fired
    private var chordLockActive = false

    fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val isDown = event.action == KeyEvent.ACTION_DOWN
        val now = event.eventTime

        if (isDown) {
            updateKeyStatus(keyCode, true)
            
            // 1. CHORD DETECTION (Instant)
            val detectedChord = getActiveChordAction()
            if (detectedChord != null) {
                if (!chordLockActive) {
                    chordLockActive = true
                    fireChord(detectedChord)
                }
                return false
            }

            // 2. SEQUENCE DETECTION (If not in chord)
            if (!chordLockActive && (now - lastDownTime > DEBOUNCE_MS)) {
                lastDownTime = now
                queueKey(keyCode)
            }
        } else {
            // Key Up
            updateKeyStatus(keyCode, false)
            
            // Reset chord lock only when ALL keys are fully released
            if (!upPressed && !downPressed && !powerPressed) {
                chordLockActive = false
            }
        }
        
        return false 
    }

    private fun updateKeyStatus(code: Int, pressed: Boolean) {
        when (code) {
            KeyEvent.KEYCODE_VOLUME_UP -> upPressed = pressed
            KeyEvent.KEYCODE_VOLUME_DOWN -> downPressed = pressed
            KeyEvent.KEYCODE_POWER -> powerPressed = pressed
        }
    }

    private fun getActiveChordAction(): String? {
        return when {
            upPressed && downPressed -> PreferencesManager.ACTION_PLAY_PAUSE
            powerPressed && upPressed -> PreferencesManager.ACTION_LAUNCH
            powerPressed && downPressed -> PreferencesManager.ACTION_HOME
            else -> null
        }
    }

    private fun queueKey(code: Int) {
        processRunnable?.let { handler.removeCallbacks(it) }
        
        sequenceBuffer.add(code)
        Logger.d("Added to Buffer: ${getKeyName(code)}")

        processRunnable = Runnable {
            val finalSequence = ArrayList(sequenceBuffer)
            sequenceBuffer.clear()
            processRunnable = null
            executeSequence(finalSequence)
        }
        handler.postDelayed(processRunnable!!, prefs.doubleTapWindowMs.toLong())
    }

    private fun executeSequence(sequence: List<Int>) {
        if (sequence.isEmpty()) return
        
        val keyString = sequence.joinToString(", ") { getKeyName(it) }
        Logger.i("Processing Sequence: [$keyString]")

        when (sequence) {
            // 2 Ups -> Next
            listOf(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_UP) -> {
                listener.onGesture(PreferencesManager.ACTION_NEXT)
            }
            // 2 Downs -> Prev
            listOf(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN) -> {
                listener.onGesture(PreferencesManager.ACTION_PREV)
            }
            // UP, DOWN -> Play/Pause (Sequence fallback for messy chords)
            listOf(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN) -> {
                listener.onGesture(PreferencesManager.ACTION_PLAY_PAUSE)
            }
            // DOWN, UP -> Launch/ReVanced (Sequence fallback)
            listOf(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP) -> {
                listener.onGesture(PreferencesManager.ACTION_LAUNCH)
            }
            else -> {
                Logger.d("Sequence [$keyString] matched no action.")
            }
        }
    }

    private fun fireChord(action: String) {
        // Kill any pending sequence timers immediately
        sequenceBuffer.clear()
        processRunnable?.let { handler.removeCallbacks(it) }
        processRunnable = null
        
        Logger.i("Chord Triggered: $action")
        listener.onGesture(action)
    }

    private fun getKeyName(code: Int): String = when (code) {
        KeyEvent.KEYCODE_VOLUME_UP -> "UP"
        KeyEvent.KEYCODE_VOLUME_DOWN -> "DOWN"
        KeyEvent.KEYCODE_POWER -> "POWER"
        else -> "KEY_$code"
    }

    fun destroy() {
        handler.removeCallbacksAndMessages(null)
    }
}
