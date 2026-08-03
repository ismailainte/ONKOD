package com.onkod.keyboard.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutTest {
    @Test
    fun qwertyOrderIsFixed() {
        val rows = KeyboardLayouts.qwerty.letterRows.map { row -> row.map { it.label } }
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
    fun ashertyOrderIsFixed() {
        val rows = KeyboardLayouts.asherty.letterRows.map { row -> row.map { it.label } }
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
    fun somaliInvariantsAreValid() {
        val labels = KeyboardLayouts.qwerty.letterRows.flatten().map { it.label } +
            KeyboardLayouts.asherty.letterRows.flatten().map { it.label }
        assertFalse(labels.contains("P"))
        assertFalse(labels.contains("V"))
        assertFalse(labels.contains("Z"))
        assertTrue(labels.contains("SH"))
        assertTrue(labels.contains("DH"))
        assertTrue(labels.contains("KH"))
        assertTrue(labels.contains("W"))
        assertEquals(emptyList<String>(), KeyboardLayouts.validate(KeyboardLayouts.qwerty))
        assertEquals(emptyList<String>(), KeyboardLayouts.validate(KeyboardLayouts.asherty))
    }

    @Test
    fun digraphOutputFollowsShiftState() {
        assertEquals("sh", outputFor("SH", ShiftState.LOWERCASE))
        assertEquals("Dh", outputFor("DH", ShiftState.SHIFT))
        assertEquals("KH", outputFor("KH", ShiftState.CAPS))
    }
}
