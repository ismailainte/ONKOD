package com.onkod.keyboard.ime

import android.content.Context
import org.json.JSONObject

class SettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("onkod_keyboard_settings", Context.MODE_PRIVATE)

    fun read(): KeyboardSettings = KeyboardSettings(
        layoutGroup = preferences.getString("layoutGroup", preferences.getString("layout", "qwerty")).toLayoutGroup(),
        qwertyLanguage = preferences.getString("qwertyLanguage", "somali").toLanguageMode(),
        ashertyLanguage = preferences.getString("ashertyLanguage", "somali").toLanguageMode(),
        theme = preferences.getString("theme", "system").toThemeMode(),
        numberRow = preferences.getBoolean("numberRow", true),
        toolbar = preferences.getBoolean("toolbar", true),
        vibration = preferences.getBoolean("vibration", true),
        sound = preferences.getBoolean("sound", false),
        keyPreview = preferences.getBoolean("keyPreview", true),
        longPressDelay = preferences.getString("longPressDelay", "normal").toLongPressDelay()
    )

    fun write(settings: KeyboardSettings): KeyboardSettings {
        preferences.edit()
            .putString("layoutGroup", settings.layoutGroup.toValue())
            .putString("qwertyLanguage", settings.qwertyLanguage.toValue())
            .putString("ashertyLanguage", settings.ashertyLanguage.toValue())
            .putString("theme", settings.theme.toValue())
            .putBoolean("numberRow", settings.numberRow)
            .putBoolean("toolbar", settings.toolbar)
            .putBoolean("vibration", settings.vibration)
            .putBoolean("sound", settings.sound)
            .putBoolean("keyPreview", settings.keyPreview)
            .putString("longPressDelay", settings.longPressDelay.toValue())
            .apply()
        return settings
    }

    fun fromJson(json: JSONObject): KeyboardSettings = KeyboardSettings(
        layoutGroup = json.optString("layoutGroup", "qwerty").toLayoutGroup(),
        qwertyLanguage = json.optString("qwertyLanguage", "somali").toLanguageMode(),
        ashertyLanguage = json.optString("ashertyLanguage", "somali").toLanguageMode(),
        theme = json.optString("theme", "system").toThemeMode(),
        numberRow = json.optBoolean("numberRow", true),
        toolbar = json.optBoolean("toolbar", true),
        vibration = json.optBoolean("vibration", true),
        sound = json.optBoolean("sound", false),
        keyPreview = json.optBoolean("keyPreview", true),
        longPressDelay = json.optString("longPressDelay", "normal").toLongPressDelay()
    )

    fun toJson(settings: KeyboardSettings): JSONObject = JSONObject()
        .put("layoutGroup", settings.layoutGroup.toValue())
        .put("qwertyLanguage", settings.qwertyLanguage.toValue())
        .put("ashertyLanguage", settings.ashertyLanguage.toValue())
        .put("theme", settings.theme.toValue())
        .put("numberRow", settings.numberRow)
        .put("toolbar", settings.toolbar)
        .put("vibration", settings.vibration)
        .put("sound", settings.sound)
        .put("keyPreview", settings.keyPreview)
        .put("longPressDelay", settings.longPressDelay.toValue())
}

fun String?.toLayoutGroup(): LayoutGroup = if (this == "asherty") LayoutGroup.ASHERTY else LayoutGroup.QWERTY
fun String?.toLanguageMode(): LanguageMode = when (this) {
    "english" -> LanguageMode.ENGLISH
    "french" -> LanguageMode.FRENCH
    else -> LanguageMode.SOMALI
}
fun String?.toThemeMode(): ThemeMode = when (this) {
    "light" -> ThemeMode.LIGHT
    "dark" -> ThemeMode.DARK
    else -> ThemeMode.SYSTEM
}
fun String?.toLongPressDelay(): LongPressDelay = when (this) {
    "short" -> LongPressDelay.SHORT
    "long" -> LongPressDelay.LONG
    else -> LongPressDelay.NORMAL
}
fun LayoutGroup.toValue(): String = if (this == LayoutGroup.ASHERTY) "asherty" else "qwerty"
fun LanguageMode.toValue(): String = when (this) {
    LanguageMode.ENGLISH -> "english"
    LanguageMode.FRENCH -> "french"
    LanguageMode.SOMALI -> "somali"
}
fun ThemeMode.toValue(): String = when (this) {
    ThemeMode.LIGHT -> "light"
    ThemeMode.DARK -> "dark"
    ThemeMode.SYSTEM -> "system"
}
fun LongPressDelay.toValue(): String = when (this) {
    LongPressDelay.SHORT -> "short"
    LongPressDelay.LONG -> "long"
    LongPressDelay.NORMAL -> "normal"
}
