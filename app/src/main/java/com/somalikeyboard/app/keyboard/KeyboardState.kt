package com.somalikeyboard.app.keyboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class KeyboardLayout { QWERTY, AZERTY, BTJ }

enum class Lang { SO, EN }

enum class Page { ABC, SYM1, SYM2, EMOJI, CLIP }

enum class ShiftState { NONE, SINGLE, CAPS }

enum class ThemeMode { DARK, LIGHT }

enum class EnterMode { ENTER, SEARCH }

enum class EmojiCategory(val id: String) {
    RECENT("recent"),
    SMILEY("smiley"),
    ANIMAL("animal"),
    FOOD("food"),
    TRAVEL("travel"),
    SPORT("sport"),
    OBJECT("object"),
    SYMBOL("symbol"),
    FLAG("flag");

    companion object {
        fun fromId(id: String): EmojiCategory = entries.firstOrNull { it.id == id } ?: SMILEY
    }
}

data class ClipItem(
    val id: String,
    val text: String,
    val pinned: Boolean = false,
)

/**
 * Mirrors the `state` object of the original design's Component class
 * (Somali Keyboard.dc.html) so the interaction logic can be ported 1:1.
 */
class KeyboardState {
    var layout by mutableStateOf(KeyboardLayout.QWERTY)
    var lang by mutableStateOf(Lang.SO)

    val clips = mutableStateListOf(
        ClipItem("c1", "Salaan diiwaangelin: 0612345678"),
        ClipItem("c2", "https://kiiboodh.so/soomaali"),
        ClipItem("c3", "Waad ku mahadsan tahay!", pinned = true),
    )

    var category by mutableStateOf(EmojiCategory.SMILEY)
    var systemDark by mutableStateOf(true)
    var themeOverride by mutableStateOf<ThemeMode?>(null)
    var page by mutableStateOf(Page.ABC)
    var shift by mutableStateOf(ShiftState.NONE)
    var charCount by mutableStateOf(0)
    var toast by mutableStateOf("")
    var haptics by mutableStateOf(true)
    var sound by mutableStateOf(false)
    var enterModeOverride by mutableStateOf<EnterMode?>(null)

    /** Derived from EditorInfo in the real IME, or fixed in the preview screen. */
    var defaultEnterMode by mutableStateOf(EnterMode.ENTER)
    var spacebarLabelSomali by mutableStateOf("Somali (SO)")

    val resolvedTheme: ThemeMode
        get() = themeOverride ?: if (systemDark) ThemeMode.DARK else ThemeMode.LIGHT

    val resolvedEnterMode: EnterMode
        get() = enterModeOverride ?: defaultEnterMode

    val spaceLabel: String
        get() = if (lang == Lang.EN) "English (US)" else spacebarLabelSomali
}
