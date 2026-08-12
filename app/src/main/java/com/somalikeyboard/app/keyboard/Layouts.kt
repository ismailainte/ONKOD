package com.somalikeyboard.app.keyboard

/**
 * A single key on the "abc"/symbol pages. `id` is what gets passed to
 * [KeyboardController.onKeyDown] (mirrors the `data-k` attribute in the source design).
 *
 * `label` is shown when [KeyboardState.lang] is Somali; `enLabel` (if present) overrides it
 * for the SH/DH/KH digraph keys when the English layer is active.
 */
data class KeySpec(
    val id: String,
    val label: String,
    val enLabel: String? = null,
    val weight: Float = 1f,
    val special: Boolean = false,
    val fontSize: Float = 20f,
    val bottomAlign: Boolean = false,
)

sealed class RowItem {
    data class K(val spec: KeySpec) : RowItem()
    data class Blank(val weight: Float) : RowItem()
}

private fun key(id: String, label: String, weight: Float = 1f, fontSize: Float = 20f) =
    RowItem.K(KeySpec(id, label, weight = weight, fontSize = fontSize))

private fun digraph(id: String, soLabel: String, enLabel: String, weight: Float = 1f) =
    RowItem.K(KeySpec(id, soLabel, enLabel, weight = weight, fontSize = 14.5f))

private fun blank(weight: Float) = RowItem.Blank(weight)

object Layouts {

    val numberRow: List<RowItem> = (1..9).map { key(it.toString(), it.toString()) } +
        key("0", "0")

    val qwerty: List<List<RowItem>> = listOf(
        listOf(
            key("q", "Q"), key("w", "W"), key("e", "E"), key("r", "R"), key("t", "T"),
            key("y", "Y"), key("u", "U"), key("i", "I"), key("o", "O"),
            digraph("kh", "KH", "P"),
        ),
        listOf(
            blank(0.5f),
            key("a", "A"), key("s", "S"), key("d", "D"), key("f", "F"), key("g", "G"),
            key("h", "H"), key("j", "J"), key("k", "K"), key("l", "L"),
            blank(0.5f),
        ),
        listOf(
            RowItem.K(KeySpec("shift", "", weight = 1.5f, special = true)),
            digraph("sh", "SH", "Z"), key("x", "X"), key("c", "C"),
            digraph("dh", "DH", "V"), key("b", "B"), key("n", "N"), key("m", "M"),
            RowItem.K(KeySpec("bs", "", weight = 1.5f, special = true)),
        ),
    )

    val azerty: List<List<RowItem>> = listOf(
        listOf(
            key("a", "A"), digraph("sh", "SH", "Z"), key("e", "E"), key("r", "R"), key("t", "T"),
            key("y", "Y"), key("u", "U"), key("i", "I"), key("o", "O"),
            digraph("kh", "KH", "P"),
        ),
        listOf(
            key("q", "Q"), key("s", "S"), key("d", "D"), key("f", "F"), key("g", "G"),
            key("h", "H"), key("j", "J"), key("k", "K"), key("l", "L"), key("m", "M"),
        ),
        listOf(
            RowItem.K(KeySpec("shift", "", weight = 1.5f, special = true)),
            blank(0.5f),
            key("w", "W"), key("x", "X"), key("c", "C"), digraph("dh", "DH", "V"),
            key("b", "B"), key("n", "N"),
            blank(0.5f),
            RowItem.K(KeySpec("bs", "", weight = 1.5f, special = true)),
        ),
    )

    val btj: List<List<RowItem>> = listOf(
        listOf(
            key("b", "B"), key("t", "T"), key("j", "J"), key("x", "X"),
            digraph("kh", "KH", "P"), key("d", "D"), key("r", "R"), key("s", "S"),
            digraph("sh", "SH", "Z"), digraph("dh", "DH", "V"),
        ),
        listOf(
            key("c", "C"), key("g", "G"), key("f", "F"), key("q", "Q"), key("k", "K"),
            key("l", "L"), key("m", "M"), key("n", "N"), key("w", "W"), key("h", "H"),
        ),
        listOf(
            RowItem.K(KeySpec("shift", "", weight = 1.5f, special = true)),
            blank(0.5f),
            key("y", "Y"), key("a", "A"), key("e", "E"), key("i", "I"), key("o", "O"),
            key("u", "U"),
            blank(0.5f),
            RowItem.K(KeySpec("bs", "", weight = 1.5f, special = true)),
        ),
    )

    fun layoutRows(layout: KeyboardLayout): List<List<RowItem>> = when (layout) {
        KeyboardLayout.QWERTY -> qwerty
        KeyboardLayout.AZERTY -> azerty
        KeyboardLayout.BTJ -> btj
    }

    /** Bottom row shared by the "abc" page: sym / globe / , / space / . / enter. */
    val abcBottomRow: List<RowItem> = listOf(
        RowItem.K(KeySpec("sym", "!#1", weight = 1.45f, special = true, fontSize = 17f)),
        RowItem.K(KeySpec("globe", "", weight = 1f, special = true)),
        RowItem.K(KeySpec(",", ",", weight = 1.1f, fontSize = 21f, bottomAlign = true)),
        RowItem.K(KeySpec("space", "", weight = 4.75f)),
        RowItem.K(KeySpec(".", ".", weight = 1.1f, fontSize = 21f, bottomAlign = true)),
        RowItem.K(KeySpec("enter", "", weight = 1.62f, special = true)),
    )

    /** Bottom row shared by the symbol pages (s1/s2): abc / globe / , / space / . / enter. */
    val symBottomRow: List<RowItem> = listOf(
        RowItem.K(KeySpec("abc", "ABC", weight = 1.45f, special = true, fontSize = 17f)),
        RowItem.K(KeySpec("globe", "", weight = 1f, special = true)),
        RowItem.K(KeySpec(",", ",", weight = 1.1f, fontSize = 21f, bottomAlign = true)),
        RowItem.K(KeySpec("space", "", weight = 4.75f)),
        RowItem.K(KeySpec(".", ".", weight = 1.1f, fontSize = 21f, bottomAlign = true)),
        RowItem.K(KeySpec("enter", "", weight = 1.62f, special = true)),
    )

    /** Bottom row for the emoji page: abc / globe / space / bs (no punctuation, no enter). */
    val emojiBottomRow: List<RowItem> = listOf(
        RowItem.K(KeySpec("abc", "ABC", weight = 1.45f, special = true, fontSize = 17f)),
        RowItem.K(KeySpec("globe", "", weight = 1f, special = true)),
        RowItem.K(KeySpec("space", "", weight = 5.9f)),
        RowItem.K(KeySpec("bs", "", weight = 1.5f, special = true)),
    )

    val symbolsPage1: List<List<RowItem>> = listOf(
        listOf(
            key("+", "+", fontSize = 19f), key("×", "×", fontSize = 19f), key("÷", "÷", fontSize = 19f),
            key("=", "=", fontSize = 19f), key("/", "/", fontSize = 19f), key("_", "_", fontSize = 19f),
            key("<", "<", fontSize = 19f), key(">", ">", fontSize = 19f),
            key("[", "[", fontSize = 19f), key("]", "]", fontSize = 19f),
        ),
        listOf(
            key("!", "!", fontSize = 19f), key("@", "@", fontSize = 19f), key("#", "#", fontSize = 19f),
            key("$", "$", fontSize = 19f), key("%", "%", fontSize = 19f), key("^", "^", fontSize = 19f),
            key("&", "&", fontSize = 19f), key("*", "*", fontSize = 19f),
            key("(", "(", fontSize = 19f), key(")", ")", fontSize = 19f),
        ),
        listOf(
            RowItem.K(KeySpec("page2", "1/2", weight = 1.5f, special = true, fontSize = 17f)),
            key("-", "-", fontSize = 19f), key("'", "'", fontSize = 19f), key("\"", "\"", fontSize = 19f),
            key(":", ":", fontSize = 19f), key(";", ";", fontSize = 19f), key(",", ",", fontSize = 19f),
            key("?", "?", fontSize = 19f),
            RowItem.K(KeySpec("bs", "", weight = 1.5f, special = true)),
        ),
    )

    val symbolsPage2: List<List<RowItem>> = listOf(
        listOf(
            key("`", "`", fontSize = 19f), key("~", "~", fontSize = 19f), key("\\", "\\", fontSize = 19f),
            key("|", "|", fontSize = 19f), key("{", "{", fontSize = 19f), key("}", "}", fontSize = 19f),
            key("€", "€", fontSize = 19f), key("£", "£", fontSize = 19f),
            key("¥", "¥", fontSize = 19f), key("₩", "₩", fontSize = 19f),
        ),
        listOf(
            key("°", "°", fontSize = 19f), key("•", "•", fontSize = 19f), key("○", "○", fontSize = 19f),
            key("●", "●", fontSize = 19f), key("□", "□", fontSize = 19f), key("■", "■", fontSize = 19f),
            key("♤", "♤", fontSize = 19f), key("♡", "♡", fontSize = 19f),
            key("◇", "◇", fontSize = 19f), key("♧", "♧", fontSize = 19f),
        ),
        listOf(
            RowItem.K(KeySpec("page1", "2/2", weight = 1.5f, special = true, fontSize = 17f)),
            key("☆", "☆", fontSize = 19f), key("▪", "▪", fontSize = 19f), key("¤", "¤", fontSize = 19f),
            key("《", "《", fontSize = 19f), key("》", "》", fontSize = 19f), key("¡", "¡", fontSize = 19f),
            key("¿", "¿", fontSize = 19f),
            RowItem.K(KeySpec("bs", "", weight = 1.5f, special = true)),
        ),
    )
}
