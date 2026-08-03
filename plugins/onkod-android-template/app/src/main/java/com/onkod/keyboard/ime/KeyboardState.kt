package com.onkod.keyboard.ime

enum class LayoutType { QWERTY, ASHERTY }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class ShiftState { LOWERCASE, SHIFT, CAPS }
enum class LongPressDelay { NORMAL, SHORT, LONG }

data class KeyboardSettings(
    val layout: LayoutType = LayoutType.QWERTY,
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
    val type: LayoutType,
    val toolbar: List<KeyboardKey>,
    val numberRow: List<KeyboardKey>,
    val letterRows: List<List<KeyboardKey>>,
    val bottomRow: List<KeyboardKey>,
    val spaceLabel: String = "Somali"
)

fun outputFor(label: String, shiftState: ShiftState): String {
    val lower = label.lowercase()
    return when (shiftState) {
        ShiftState.LOWERCASE -> lower
        ShiftState.SHIFT -> lower.replaceFirstChar { it.uppercase() }
        ShiftState.CAPS -> label.uppercase()
    }
}
