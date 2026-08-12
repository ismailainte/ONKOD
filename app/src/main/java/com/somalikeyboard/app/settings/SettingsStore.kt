package com.somalikeyboard.app.settings

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "somali_keyboard_settings"
private const val KEY_HAPTICS = "haptics"
private const val KEY_SOUND = "sound"

/**
 * Persists the haptics/sound toggles so a choice made in [com.somalikeyboard.app.MainActivity]'s
 * preview footer also applies to the real system keyboard, and vice versa.
 */
class SettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var haptics: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var sound: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
