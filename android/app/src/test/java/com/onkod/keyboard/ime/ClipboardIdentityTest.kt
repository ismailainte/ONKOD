package com.onkod.keyboard.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ClipboardIdentityTest {
    @Test
    fun textIdentityIsStableForSameClipboardItem() {
        val first = ClipboardIdentity.text("text/plain", "hello", 123L)
        val second = ClipboardIdentity.text("text/plain", "hello", 123L)

        assertEquals(first, second)
        assertFalse(first.contains("hello"))
    }

    @Test
    fun textIdentityChangesForNewTextOrTimestamp() {
        val original = ClipboardIdentity.text("text/plain", "hello", 123L)

        assertNotEquals(original, ClipboardIdentity.text("text/plain", "world", 123L))
        assertNotEquals(original, ClipboardIdentity.text("text/plain", "hello", 124L))
    }

    @Test
    fun imageIdentityChangesForNewUriOrMimeType() {
        val original = ClipboardIdentity.image("image/png", "image/png", "content://screenshots/1", 123L)

        assertNotEquals(
            original,
            ClipboardIdentity.image("image/png", "image/png", "content://screenshots/2", 123L)
        )
        assertNotEquals(
            original,
            ClipboardIdentity.image("image/jpeg", "image/jpeg", "content://screenshots/1", 123L)
        )
    }
}
