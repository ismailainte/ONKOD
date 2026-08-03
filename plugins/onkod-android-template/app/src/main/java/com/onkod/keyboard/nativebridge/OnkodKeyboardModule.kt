package com.onkod.keyboard.nativebridge

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.onkod.keyboard.ime.KeyboardSettings
import com.onkod.keyboard.ime.SettingsStore
import com.onkod.keyboard.ime.toLayoutType
import com.onkod.keyboard.ime.toLongPressDelay
import com.onkod.keyboard.ime.toThemeMode

class OnkodKeyboardModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    private val settingsStore = SettingsStore(reactContext)

    override fun getName(): String = "OnkodKeyboard"

    @ReactMethod
    fun openInputMethodSettings(promise: Promise) {
        try {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            reactContext.startActivity(intent)
            promise.resolve(null)
        } catch (error: Exception) {
            promise.reject("ONKOD_SETTINGS_ERROR", error)
        }
    }

    @ReactMethod
    fun showInputMethodPicker(promise: Promise) {
        try {
            val manager = reactContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showInputMethodPicker()
            promise.resolve(null)
        } catch (error: Exception) {
            promise.reject("ONKOD_PICKER_ERROR", error)
        }
    }

    @ReactMethod
    fun isOnkodKeyboardEnabled(promise: Promise) {
        val enabled = Settings.Secure.getString(reactContext.contentResolver, Settings.Secure.ENABLED_INPUT_METHODS)
            .orEmpty()
            .contains("${reactContext.packageName}/")
        promise.resolve(enabled)
    }

    @ReactMethod
    fun getKeyboardSettings(promise: Promise) {
        promise.resolve(toWritableMap(settingsStore.read()))
    }

    @ReactMethod
    fun updateKeyboardSettings(settings: ReadableMap, promise: Promise) {
        val parsed = KeyboardSettings(
            layout = settings.getString("layout").toLayoutType(),
            theme = settings.getString("theme").toThemeMode(),
            numberRow = if (settings.hasKey("numberRow")) settings.getBoolean("numberRow") else true,
            toolbar = if (settings.hasKey("toolbar")) settings.getBoolean("toolbar") else true,
            vibration = if (settings.hasKey("vibration")) settings.getBoolean("vibration") else true,
            sound = if (settings.hasKey("sound")) settings.getBoolean("sound") else false,
            keyPreview = if (settings.hasKey("keyPreview")) settings.getBoolean("keyPreview") else true,
            longPressDelay = settings.getString("longPressDelay").toLongPressDelay()
        )
        promise.resolve(toWritableMap(settingsStore.write(parsed)))
    }

    private fun toWritableMap(settings: KeyboardSettings): WritableMap =
        Arguments.createMap().apply {
            putString("layout", if (settings.layout.name == "ASHERTY") "asherty" else "qwerty")
            putString("theme", settings.theme.name.lowercase())
            putBoolean("numberRow", settings.numberRow)
            putBoolean("toolbar", settings.toolbar)
            putBoolean("vibration", settings.vibration)
            putBoolean("sound", settings.sound)
            putBoolean("keyPreview", settings.keyPreview)
            putString("longPressDelay", settings.longPressDelay.name.lowercase())
        }
}
