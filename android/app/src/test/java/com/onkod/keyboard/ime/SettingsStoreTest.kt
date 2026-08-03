package com.onkod.keyboard.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsStoreTest {
    @Test
    fun parsesSettingValuesSafely() {
        assertEquals(LayoutGroup.ASHERTY, "asherty".toLayoutGroup())
        assertEquals(LayoutGroup.QWERTY, "unknown".toLayoutGroup())
        assertEquals(LanguageMode.ENGLISH, "english".toLanguageMode())
        assertEquals(LanguageMode.FRENCH, "french".toLanguageMode())
        assertEquals(LanguageMode.SOMALI, "unknown".toLanguageMode())
        assertEquals(ThemeMode.DARK, "dark".toThemeMode())
        assertEquals(ThemeMode.SYSTEM, "unknown".toThemeMode())
        assertEquals(LongPressDelay.SHORT, "short".toLongPressDelay())
        assertEquals(LongPressDelay.NORMAL, null.toLongPressDelay())
    }
}
