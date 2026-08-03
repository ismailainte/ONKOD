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

    private fun bottomRow(spaceLabel: String) = listOf(
        KeyboardKey("!#1", KeyAction.Symbols, 1f),
        KeyboardKey("Globe", KeyAction.Globe, 1f),
        KeyboardKey(spaceLabel, KeyAction.Space, 4f),
        KeyboardKey(".", KeyAction.Period, 1f),
        KeyboardKey("Hide", KeyAction.HideKeyboard, 1.2f)
    )

    val somaliQwerty = KeyboardLayout(
        mode = KeyboardMode.SOMALI_QWERTY,
        toolbar = toolbar,
        numberRow = numberRow,
        letterRows = listOf(
            row("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "KH"),
            row("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            listOf(KeyboardKey("Shift", KeyAction.Shift, 1.25f)) +
                row("SH", "X", "C", "DH", "B", "N", "M") +
                KeyboardKey("Backspace", KeyAction.Backspace, 1.35f)
        ),
        bottomRow = bottomRow("Somali"),
        spaceLabel = "Somali"
    )

    val englishQwerty = KeyboardLayout(
        mode = KeyboardMode.ENGLISH_QWERTY,
        toolbar = toolbar,
        numberRow = numberRow,
        letterRows = listOf(
            row("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            row("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            listOf(KeyboardKey("Shift", KeyAction.Shift, 1.25f)) +
                row("Z", "X", "C", "V", "B", "N", "M") +
                KeyboardKey("Backspace", KeyAction.Backspace, 1.35f)
        ),
        bottomRow = bottomRow("English"),
        spaceLabel = "English"
    )

    val somaliAsherty = KeyboardLayout(
        mode = KeyboardMode.SOMALI_ASHERTY,
        toolbar = toolbar,
        numberRow = numberRow,
        letterRows = listOf(
            row("A", "SH", "E", "R", "T", "Y", "U", "I", "O", "KH"),
            row("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"),
            listOf(KeyboardKey("Shift", KeyAction.Shift, 1.25f)) +
                row("W", "X", "C", "DH", "B", "N") +
                KeyboardKey("Backspace", KeyAction.Backspace, 1.35f)
        ),
        bottomRow = bottomRow("Somali"),
        spaceLabel = "Somali"
    )

    val frenchAzerty = KeyboardLayout(
        mode = KeyboardMode.FRENCH_AZERTY,
        toolbar = toolbar,
        numberRow = numberRow,
        letterRows = listOf(
            row("A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P"),
            row("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"),
            listOf(KeyboardKey("Shift", KeyAction.Shift, 1.25f)) +
                row("W", "X", "C", "V", "B", "N") +
                KeyboardKey("Backspace", KeyAction.Backspace, 1.35f)
        ),
        bottomRow = bottomRow("Français"),
        spaceLabel = "Français",
        longPressOptions = mapOf(
            "A" to listOf("à", "â", "æ"),
            "C" to listOf("ç"),
            "E" to listOf("é", "è", "ê", "ë"),
            "I" to listOf("î", "ï"),
            "O" to listOf("ô", "œ"),
            "U" to listOf("ù", "û", "ü"),
            "Y" to listOf("ÿ")
        )
    )

    val symbolsRows = listOf(
        row("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        row("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
        row("*", "\"", "'", ":", ";", "!", "?"),
        row(",", ".", "_", "=", "<", ">")
    )

    fun forMode(mode: KeyboardMode): KeyboardLayout = when (mode) {
        KeyboardMode.SOMALI_QWERTY -> somaliQwerty
        KeyboardMode.ENGLISH_QWERTY -> englishQwerty
        KeyboardMode.SOMALI_ASHERTY -> somaliAsherty
        KeyboardMode.FRENCH_AZERTY -> frenchAzerty
    }

    fun validate(layout: KeyboardLayout): List<String> {
        val labels = layout.letterRows.flatten().map { it.label }
        val errors = mutableListOf<String>()
        when (layout.mode) {
            KeyboardMode.SOMALI_QWERTY, KeyboardMode.SOMALI_ASHERTY -> {
                listOf("P", "V", "Z").forEach {
                    if (labels.contains(it)) errors += "$it must not be a primary Somali key"
                }
                listOf("SH", "DH", "KH").forEach {
                    if (!labels.contains(it)) errors += "$it is required"
                }
                if (layout.mode == KeyboardMode.SOMALI_ASHERTY && !labels.contains("W")) {
                    errors += "Somali ASHERTY must contain W"
                }
                if (layout.spaceLabel != "Somali") errors += "Somali spacebar label must be Somali"
            }
            KeyboardMode.ENGLISH_QWERTY -> {
                listOf("P", "V", "Z").forEach {
                    if (!labels.contains(it)) errors += "English QWERTY must contain $it"
                }
                if (layout.spaceLabel != "English") errors += "English spacebar label must be English"
            }
            KeyboardMode.FRENCH_AZERTY -> {
                listOf("P", "V", "Z", "W").forEach {
                    if (!labels.contains(it)) errors += "French AZERTY must contain $it"
                }
                if (layout.letterRows.first().take(6).map { it.label } != listOf("A", "Z", "E", "R", "T", "Y")) {
                    errors += "French AZERTY must begin with A Z E R T Y"
                }
                if (layout.spaceLabel != "Français") errors += "French spacebar label must be Français"
            }
        }
        return errors
    }

    private fun row(vararg labels: String): List<KeyboardKey> = labels.map { KeyboardKey(it, KeyAction.Text(it)) }
}
