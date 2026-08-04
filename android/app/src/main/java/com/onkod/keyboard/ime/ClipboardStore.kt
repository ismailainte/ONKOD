package com.onkod.keyboard.ime

import android.content.Context
import org.json.JSONArray

class ClipboardStore(context: Context) {
    private val preferences = context.getSharedPreferences("onkod_clipboard", Context.MODE_PRIVATE)

    fun readPinned(): List<String> {
        val raw = preferences.getString("pinned", "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index -> array.optString(index) }
                .filter { it.isNotBlank() }
                .distinct()
        }.getOrDefault(emptyList())
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

    private fun writePinned(values: List<String>) {
        preferences.edit()
            .putString("pinned", JSONArray(values).toString())
            .apply()
    }
}
