package com.onkod.keyboard.ime

enum class LayoutGroup { QWERTY, ASHERTY }
enum class LanguageMode { SOMALI, ENGLISH, FRENCH }
enum class KeyboardMode { SOMALI_QWERTY, ENGLISH_QWERTY, SOMALI_ASHERTY, FRENCH_AZERTY }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class ShiftState { LOWERCASE, SHIFT, CAPS }
enum class LongPressDelay { NORMAL, SHORT, LONG }
enum class EmojiCategory { HISTORY, FACES, ANIMALS, FOOD, HOME, SPORTS, BOOKS, SYMBOLS, FLAGS }

data class KeyboardSettings(
    val layoutGroup: LayoutGroup = LayoutGroup.QWERTY,
    val qwertyLanguage: LanguageMode = LanguageMode.SOMALI,
    val ashertyLanguage: LanguageMode = LanguageMode.SOMALI,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val numberRow: Boolean = true,
    val toolbar: Boolean = true,
    val vibration: Boolean = true,
    val sound: Boolean = false,
    val keyPreview: Boolean = true,
    val longPressDelay: LongPressDelay = LongPressDelay.NORMAL
)

data class KeyboardKey(
    val label: String,
    val action: KeyAction,
    val weight: Float = 1f
)

sealed class KeyAction {
    data class Text(val value: String) : KeyAction()
    data class EmojiCategorySelect(val category: EmojiCategory) : KeyAction()
    data object Shift : KeyAction()
    data object Backspace : KeyAction()
    data object Symbols : KeyAction()
    data object Globe : KeyAction()
    data object Space : KeyAction()
    data object Period : KeyAction()
    data object Enter : KeyAction()
    data object HideKeyboard : KeyAction()
    data object Abc : KeyAction()
    data object EmojiPanel : KeyAction()
    data object Settings : KeyAction()
    data object Clipboard : KeyAction()
    data object More : KeyAction()
}

data class KeyboardLayout(
    val mode: KeyboardMode,
    val toolbar: List<KeyboardKey>,
    val numberRow: List<KeyboardKey>,
    val letterRows: List<List<KeyboardKey>>,
    val bottomRow: List<KeyboardKey>,
    val spaceLabel: String,
    val longPressOptions: Map<String, List<String>> = emptyMap()
)

fun KeyboardSettings.activeMode(): KeyboardMode = when (layoutGroup) {
    LayoutGroup.QWERTY -> if (qwertyLanguage == LanguageMode.ENGLISH) {
        KeyboardMode.ENGLISH_QWERTY
    } else {
        KeyboardMode.SOMALI_QWERTY
    }
    LayoutGroup.ASHERTY -> if (ashertyLanguage == LanguageMode.FRENCH) {
        KeyboardMode.FRENCH_AZERTY
    } else {
        KeyboardMode.SOMALI_ASHERTY
    }
}

fun KeyboardSettings.nextInternalLanguage(): KeyboardSettings = when (layoutGroup) {
    LayoutGroup.QWERTY -> copy(
        qwertyLanguage = if (qwertyLanguage == LanguageMode.SOMALI) LanguageMode.ENGLISH else LanguageMode.SOMALI
    )
    LayoutGroup.ASHERTY -> copy(
        ashertyLanguage = if (ashertyLanguage == LanguageMode.SOMALI) LanguageMode.FRENCH else LanguageMode.SOMALI
    )
}

fun outputFor(label: String, shiftState: ShiftState): String {
    val lower = label.lowercase()
    return when (shiftState) {
        ShiftState.LOWERCASE -> lower
        ShiftState.SHIFT -> lower.replaceFirstChar { it.uppercase() }
        ShiftState.CAPS -> label.uppercase()
    }
}
