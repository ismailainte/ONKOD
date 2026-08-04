package com.onkod.keyboard.ime

import android.content.Context
import org.json.JSONArray

class ClipboardStore(context: Context) {
    private val preferences = context.getSharedPreferences("onkod_clipboard", Context.MODE_PRIVATE)

    fun readPinned(): List<String> {
        return readList("pinned")
    }

    fun readRecent(): List<String> {
        return readList("recent")
    }

    fun rememberRecent(value: String): List<String> {
        val cleanValue = value.trim()
        if (cleanValue.isBlank()) return readRecent()
        val recent = (listOf(cleanValue) + readRecent().filterNot { it == cleanValue }).take(24)
        writeList("recent", recent)
        return recent
    }

    fun pin(value: String): List<String> {
        val cleanValue = value.trim()
        if (cleanValue.isBlank()) return readPinned()
        val pinned = (listOf(cleanValue) + readPinned().filterNot { it == cleanValue }).take(24)
        writePinned(pinned)
        return pinned
    }

    fun unpin(value: String): List<String> {
        val pinned = readPinned().filterNot { it == value }
        writePinned(pinned)
        return pinned
    }

    fun replacePinned(values: List<String>): List<String> {
        val pinned = values
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(24)
        writePinned(pinned)
        return pinned
    }

    fun delete(values: Set<String>) {
        if (values.isEmpty()) return
        writeList("recent", readRecent().filterNot { values.contains(it) })
        writePinned(readPinned().filterNot { values.contains(it) })
    }

    fun clearRecent(): List<String> {
        writeList("recent", emptyList())
        return emptyList()
    }

    private fun writePinned(values: List<String>) {
        writeList("pinned", values)
    }

    private fun readList(key: String): List<String> {
        val raw = preferences.getString(key, "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index -> array.optString(index) }
                .filter { it.isNotBlank() }
                .distinct()
        }.getOrDefault(emptyList())
    }

    private fun writeList(key: String, values: List<String>) {
        preferences.edit()
            .putString(key, JSONArray(values).toString())
            .apply()
    }
}
