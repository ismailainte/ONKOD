package com.onkod.keyboard.ime

object KeyboardLayouts {
    private val toolbar = listOf(
        KeyboardKey("Emoji", KeyAction.EmojiPanel),
        KeyboardKey("Settings", KeyAction.Settings),
        KeyboardKey("Clipboard", KeyAction.Clipboard),
        KeyboardKey("More", KeyAction.More)
    )

    private val numberRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        .map { KeyboardKey(it, KeyAction.Text(it)) }

    private val bottomRow = listOf(
        KeyboardKey("!#1", KeyAction.Symbols, 1f),
        KeyboardKey("Globe", KeyAction.Globe, 1f),
        KeyboardKey("Somali", KeyAction.Space, 4f),
        KeyboardKey(".", KeyAction.Period, 1f),
        KeyboardKey("Hide", KeyAction.HideKeyboard, 1.2f)
    )

    val qwerty = KeyboardLayout(
        type = LayoutType.QWERTY,
        toolbar = toolbar,
        numberRow = numberRow,
        letterRows = listOf(
            row("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "KH"),
            row("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            listOf(KeyboardKey("Shift", KeyAction.Shift, 1.25f)) +
                row("SH", "X", "C", "DH", "B", "N", "M") +
                KeyboardKey("Backspace", KeyAction.Backspace, 1.35f)
        ),
        bottomRow = bottomRow
    )

    val asherty = KeyboardLayout(
        type = LayoutType.ASHERTY,
        toolbar = toolbar,
        numberRow = numberRow,
        letterRows = listOf(
            row("A", "SH", "E", "R", "T", "Y", "U", "I", "O", "KH"),
            row("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"),
            listOf(KeyboardKey("Shift", KeyAction.Shift, 1.25f)) +
                row("W", "X", "C", "DH", "B", "N") +
                KeyboardKey("Backspace", KeyAction.Backspace, 1.35f)
        ),
        bottomRow = bottomRow
    )

    val symbolsRows = listOf(
        row("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        row("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
        row("*", "\"", "'", ":", ";", "!", "?"),
        row(",", ".", "_", "=", "<", ">")
    )

    fun forType(type: LayoutType): KeyboardLayout = if (type == LayoutType.ASHERTY) asherty else qwerty

    fun validate(layout: KeyboardLayout): List<String> {
        val labels = layout.letterRows.flatten().map { it.label }
        val errors = mutableListOf<String>()
        listOf("P", "V", "Z").forEach {
            if (labels.contains(it)) errors += "$it must not be a primary Somali key"
        }
        listOf("SH", "DH", "KH").forEach {
            if (!labels.contains(it)) errors += "$it is required"
        }
        if (layout.type == LayoutType.ASHERTY && !labels.contains("W")) {
            errors += "ASHERTY must contain W"
        }
        if (layout.spaceLabel != "Somali") {
            errors += "Spacebar label must be Somali"
        }
        return errors
    }

    private fun row(vararg labels: String): List<KeyboardKey> = labels.map { KeyboardKey(it, KeyAction.Text(it)) }
}
