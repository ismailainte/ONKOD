package com.onkod.keyboard.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutTest {
    @Test
    fun somaliQwertyOrderIsFixed() {
        val rows = KeyboardLayouts.somaliQwerty.letterRows.map { row -> row.map { it.label } }
        assertEquals(
            listOf(
                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "KH"),
                listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                listOf("Shift", "SH", "X", "C", "DH", "B", "N", "M", "Backspace")
            ),
            rows
        )
    }

    @Test
    fun englishQwertyOrderIsFixed() {
        val rows = KeyboardLayouts.englishQwerty.letterRows.map { row -> row.map { it.label } }
        assertEquals(
            listOf(
                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
                listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                listOf("Shift", "Z", "X", "C", "V", "B", "N", "M", "Backspace")
            ),
            rows
        )
    }

    @Test
    fun somaliAshertyOrderIsFixed() {
        val rows = KeyboardLayouts.somaliAsherty.letterRows.map { row -> row.map { it.label } }
        assertEquals(
            listOf(
                listOf("A", "SH", "E", "R", "T", "Y", "U", "I", "O", "KH"),
                listOf("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"),
                listOf("Shift", "W", "X", "C", "DH", "B", "N", "Backspace")
            ),
            rows
        )
    }

    @Test
    fun frenchAzertyOrderIsFixed() {
        val rows = KeyboardLayouts.frenchAzerty.letterRows.map { row -> row.map { it.label } }
        assertEquals(
            listOf(
                listOf("A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P"),
                listOf("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"),
                listOf("Shift", "W", "X", "C", "V", "B", "N", "Backspace")
            ),
            rows
        )
    }

    @Test
    fun languageInvariantsAreValid() {
        val somaliLabels = KeyboardLayouts.somaliQwerty.letterRows.flatten().map { it.label } +
            KeyboardLayouts.somaliAsherty.letterRows.flatten().map { it.label }
        val englishLabels = KeyboardLayouts.englishQwerty.letterRows.flatten().map { it.label }
        val frenchLabels = KeyboardLayouts.frenchAzerty.letterRows.flatten().map { it.label }
        assertFalse(somaliLabels.contains("P"))
        assertFalse(somaliLabels.contains("V"))
        assertFalse(somaliLabels.contains("Z"))
        assertTrue(somaliLabels.contains("SH"))
        assertTrue(somaliLabels.contains("DH"))
        assertTrue(somaliLabels.contains("KH"))
        listOf("P", "V", "Z").forEach {
            assertTrue(englishLabels.contains(it))
            assertTrue(frenchLabels.contains(it))
        }
        assertTrue(KeyboardLayouts.somaliAsherty.letterRows.flatten().map { it.label }.contains("W"))
        assertEquals(emptyList<String>(), KeyboardLayouts.validate(KeyboardLayouts.somaliQwerty))
        assertEquals(emptyList<String>(), KeyboardLayouts.validate(KeyboardLayouts.englishQwerty))
        assertEquals(emptyList<String>(), KeyboardLayouts.validate(KeyboardLayouts.somaliAsherty))
        assertEquals(emptyList<String>(), KeyboardLayouts.validate(KeyboardLayouts.frenchAzerty))
    }

    @Test
    fun outputAndLanguageSwitchingAreCorrect() {
        assertEquals("sh", outputFor("SH", ShiftState.LOWERCASE))
        assertEquals("Dh", outputFor("DH", ShiftState.SHIFT))
        assertEquals("KH", outputFor("KH", ShiftState.CAPS))
        assertEquals(KeyboardMode.SOMALI_QWERTY, KeyboardSettings().activeMode())
        assertEquals(KeyboardMode.ENGLISH_QWERTY, KeyboardSettings().nextInternalLanguage().activeMode())
        assertEquals(
            KeyboardMode.FRENCH_AZERTY,
            KeyboardSettings(layoutGroup = LayoutGroup.ASHERTY).nextInternalLanguage().activeMode()
        )
    }

    @Test
    fun frenchAccentMappingsArePresent() {
        assertEquals(listOf("à", "â", "æ"), KeyboardLayouts.frenchAzerty.longPressOptions["A"])
        assertEquals(listOf("ç"), KeyboardLayouts.frenchAzerty.longPressOptions["C"])
        assertEquals(listOf("é", "è", "ê", "ë"), KeyboardLayouts.frenchAzerty.longPressOptions["E"])
        assertEquals(listOf("î", "ï"), KeyboardLayouts.frenchAzerty.longPressOptions["I"])
        assertEquals(listOf("ô", "œ"), KeyboardLayouts.frenchAzerty.longPressOptions["O"])
        assertEquals(listOf("ù", "û", "ü"), KeyboardLayouts.frenchAzerty.longPressOptions["U"])
        assertEquals(listOf("ÿ"), KeyboardLayouts.frenchAzerty.longPressOptions["Y"])
    }
}
