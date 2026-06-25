package com.mediakeys.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Single source of truth for all user preferences.
 * Uses SharedPreferences for lightweight, synchronous access.
 */
class PreferencesManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    // ── Service ──────────────────────────────────────────────────────────────

    var serviceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(v) = prefs.edit { putBoolean(KEY_SERVICE_ENABLED, v) }

    var startOnBoot: Boolean
        get() = prefs.getBoolean(KEY_BOOT_START, true)
        set(v) = prefs.edit { putBoolean(KEY_BOOT_START, v) }

    // ── Gesture timing ───────────────────────────────────────────────────────

    /** Double-tap window in milliseconds. Default 500 ms. */
    var doubleTapWindowMs: Int
        get() = prefs.getInt(KEY_DOUBLE_TAP_WINDOW, 500)
        set(v) = prefs.edit { putInt(KEY_DOUBLE_TAP_WINDOW, v) }

    /** Sequential gesture window in milliseconds (Vol Up → Vol Down). Default 500 ms. */
    var sequenceWindowMs: Int
        get() = prefs.getInt(KEY_SEQUENCE_WINDOW, 500)
        set(v) = prefs.edit { putInt(KEY_SEQUENCE_WINDOW, v) }

    /** Debounce: ignore events faster than this (ms). */
    var debounceMs: Int
        get() = prefs.getInt(KEY_DEBOUNCE, 80)
        set(v) = prefs.edit { putInt(KEY_DEBOUNCE, v) }

    // ── Action mappings ──────────────────────────────────────────────────────

    var actionVolUpDouble: String
        get() = prefs.getString(KEY_ACTION_VOL_UP_DOUBLE, ACTION_NEXT) ?: ACTION_NEXT
        set(v) = prefs.edit { putString(KEY_ACTION_VOL_UP_DOUBLE, v) }

    var actionVolDownDouble: String
        get() = prefs.getString(KEY_ACTION_VOL_DOWN_DOUBLE, ACTION_PREV) ?: ACTION_PREV
        set(v) = prefs.edit { putString(KEY_ACTION_VOL_DOWN_DOUBLE, v) }

    var actionVolUpThenDown: String
        get() = prefs.getString(KEY_ACTION_VOL_UP_DOWN, ACTION_PLAY_PAUSE) ?: ACTION_PLAY_PAUSE
        set(v) = prefs.edit { putString(KEY_ACTION_VOL_UP_DOWN, v) }

    var actionVolDownThenUp: String
        get() = prefs.getString(KEY_ACTION_VOL_DOWN_UP, ACTION_LAUNCH) ?: ACTION_LAUNCH
        set(v) = prefs.edit { putString(KEY_ACTION_VOL_DOWN_UP, v) }

    // ── Preferred media app ──────────────────────────────────────────────────

    var preferredMediaApp: String
        get() = prefs.getString(KEY_PREFERRED_APP, APP_YOUTUBE_MUSIC) ?: APP_YOUTUBE_MUSIC
        set(v) = prefs.edit { putString(KEY_PREFERRED_APP, v) }

    // ── Experimental ─────────────────────────────────────────────────────────

    var experimentalGesturesEnabled: Boolean
        get() = prefs.getBoolean(KEY_EXPERIMENTAL, false)
        set(v) = prefs.edit { putBoolean(KEY_EXPERIMENTAL, v) }

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private const val PREF_FILE = "mediakeys_prefs"

        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_BOOT_START = "boot_start"
        private const val KEY_DOUBLE_TAP_WINDOW = "double_tap_window_ms"
        private const val KEY_SEQUENCE_WINDOW = "sequence_window_ms"
        private const val KEY_DEBOUNCE = "debounce_ms"
        private const val KEY_ACTION_VOL_UP_DOUBLE = "action_vol_up_double"
        private const val KEY_ACTION_VOL_DOWN_DOUBLE = "action_vol_down_double"
        private const val KEY_ACTION_VOL_UP_DOWN = "action_vol_up_down"
        private const val KEY_ACTION_VOL_DOWN_UP = "action_vol_down_up"
        private const val KEY_PREFERRED_APP = "preferred_media_app"
        private const val KEY_EXPERIMENTAL = "experimental_gestures"

        // Action constants
        const val ACTION_NEXT = "next"
        const val ACTION_PREV = "prev"
        const val ACTION_PLAY_PAUSE = "play_pause"
        const val ACTION_LAUNCH = "launch"
        const val ACTION_HOME = "home"

        // Media app package names
        const val APP_YOUTUBE_MUSIC = "com.google.android.apps.youtube.music"
        const val APP_REVANCED_MUSIC = "app.revanced.android.apps.youtube.music"
        const val APP_RVX_MUSIC = "app.rvx.android.apps.youtube.music"
        const val APP_SPOTIFY = "com.spotify.music"

        @Volatile private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager =
            instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
    }
}
