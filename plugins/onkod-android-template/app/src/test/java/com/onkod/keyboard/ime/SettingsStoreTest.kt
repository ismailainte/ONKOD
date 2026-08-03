package com.onkod.keyboard.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsStoreTest {
    @Test
    fun parsesSettingValuesSafely() {
        assertEquals(LayoutType.ASHERTY, "asherty".toLayoutType())
        assertEquals(LayoutType.QWERTY, "unknown".toLayoutType())
        assertEquals(ThemeMode.DARK, "dark".toThemeMode())
        assertEquals(ThemeMode.SYSTEM, "unknown".toThemeMode())
        assertEquals(LongPressDelay.SHORT, "short".toLongPressDelay())
        assertEquals(LongPressDelay.NORMAL, null.toLongPressDelay())
    }
}
