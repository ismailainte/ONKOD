package com.onkod.keyboard.ime

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ClipboardStore(context: Context) {
    private val preferences = context.getSharedPreferences("onkod_clipboard", Context.MODE_PRIVATE)

    fun readPinned(): List<String> =
        readPinnedClips().mapNotNull { (it as? ClipboardClip.Text)?.value }

    fun readRecent(): List<String> =
        readRecentClips().mapNotNull { (it as? ClipboardClip.Text)?.value }

    fun readRecentImages(): List<ClipboardImage> =
        readRecentClips().mapNotNull { (it as? ClipboardClip.Image)?.image }

    fun readPinnedClips(): List<ClipboardClip> =
        readClipList("pinned_clips").ifEmpty {
            readList("pinned").map { ClipboardClip.Text(it) }
        }

    fun readRecentClips(): List<ClipboardClip> {
        val unified = readClipList("recent_clips")
        if (unified.isNotEmpty()) return unified
        return (readRecentImagesLegacy().map { ClipboardClip.Image(it) } + readList("recent").map { ClipboardClip.Text(it) })
            .distinctBy { it.id }
            .take(MAX_CLIPS)
    }

    fun rememberRecent(value: String): List<String> {
        val cleanValue = value.trim()
        if (cleanValue.isBlank()) return readRecent()
        rememberClip(ClipboardClip.Text(cleanValue))
        return readRecent()
    }

    fun rememberImage(image: ClipboardImage): List<ClipboardImage> {
        if (image.uri.isBlank() || !image.mimeType.startsWith("image/")) return readRecentImages()
        rememberClip(ClipboardClip.Image(image))
        return readRecentImages()
    }

    fun rememberClip(clip: ClipboardClip): List<ClipboardClip> {
        val recent = (listOf(clip) + readRecentClips().filterNot { it.id == clip.id }).take(MAX_CLIPS)
        writeClips("recent_clips", recent)
        writeLegacyMirrors(recent, readPinnedClips())
        return recent
    }

    fun pin(value: String): List<String> {
        val cleanValue = value.trim()
        if (cleanValue.isBlank()) return readPinned()
        pinClip(ClipboardClip.Text(cleanValue))
        return readPinned()
    }

    fun pinClip(clip: ClipboardClip): List<ClipboardClip> {
        val pinned = (listOf(clip) + readPinnedClips().filterNot { it.id == clip.id }).take(MAX_CLIPS)
        writeClips("pinned_clips", pinned)
        writeLegacyMirrors(readRecentClips(), pinned)
        return pinned
    }

    fun unpin(value: String): List<String> {
        unpinClip(ClipboardClip.Text(value))
        return readPinned()
    }

    fun unpinClip(clip: ClipboardClip): List<ClipboardClip> {
        val pinned = readPinnedClips().filterNot { it.id == clip.id }
        writeClips("pinned_clips", pinned)
        writeLegacyMirrors(readRecentClips(), pinned)
        return pinned
    }

    fun replacePinned(values: List<String>): List<String> {
        replacePinnedClips(values.map { ClipboardClip.Text(it.trim()) }.filter { it.value.isNotBlank() })
        return readPinned()
    }

    fun replacePinnedClips(values: List<ClipboardClip>): List<ClipboardClip> {
        val pinned = values.distinctBy { it.id }.take(MAX_CLIPS)
        writeClips("pinned_clips", pinned)
        writeLegacyMirrors(readRecentClips(), pinned)
        return pinned
    }

    fun delete(values: Set<String>) {
        if (values.isEmpty()) return
        deleteClips(readRecentClips().filterIsInstance<ClipboardClip.Text>().filter { values.contains(it.value) }.map { it.id }.toSet())
    }

    fun deleteClips(ids: Set<String>) {
        if (ids.isEmpty()) return
        val recent = readRecentClips().filterNot { ids.contains(it.id) }
        val pinned = readPinnedClips().filterNot { ids.contains(it.id) }
        writeClips("recent_clips", recent)
        writeClips("pinned_clips", pinned)
        writeLegacyMirrors(recent, pinned)
    }

    fun clearRecent(): List<String> {
        writeClips("recent_clips", emptyList())
        writeList("recent", emptyList())
        writeImages(emptyList())
        return emptyList()
    }

    fun readLastAutomaticallySuggestedClipboardId(): String? =
        preferences.getString("last_auto_suggested_clipboard_id", null)

    fun rememberAutomaticallySuggestedClipboardId(id: String) {
        preferences.edit()
            .putString("last_auto_suggested_clipboard_id", id)
            .apply()
    }

    fun readLastConsumedClipboardId(): String? =
        preferences.getString("last_consumed_clipboard_id", null)

    fun rememberConsumedClipboardId(id: String) {
        preferences.edit()
            .putString("last_consumed_clipboard_id", id)
            .apply()
    }

    fun readLastScreenshotMarker(): Long =
        preferences.getLong("last_screenshot_marker", 0L)

    fun rememberLastScreenshotMarker(marker: Long) {
        preferences.edit()
            .putLong("last_screenshot_marker", marker)
            .apply()
    }

    private fun readClipList(key: String): List<ClipboardClip> {
        val raw = preferences.getString(key, "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index -> array.optJSONObject(index).toClipboardClipOrNull() }
                .filterNotNull()
                .distinctBy { it.id }
        }.getOrDefault(emptyList())
    }

    private fun JSONObject?.toClipboardClipOrNull(): ClipboardClip? {
        if (this == null) return null
        return when (optString("type")) {
            "text" -> optString("value").takeIf { it.isNotBlank() }?.let { ClipboardClip.Text(it) }
            "image" -> {
                val uri = optString("uri")
                val mimeType = optString("mimeType")
                if (uri.isBlank() || !mimeType.startsWith("image/")) null else ClipboardClip.Image(ClipboardImage(uri, mimeType))
            }
            else -> null
        }
    }

    private fun writeClips(key: String, values: List<ClipboardClip>) {
        val array = JSONArray()
        values.forEach { clip ->
            array.put(JSONObject().apply {
                when (clip) {
                    is ClipboardClip.Text -> {
                        put("type", "text")
                        put("value", clip.value)
                    }
                    is ClipboardClip.Image -> {
                        put("type", "image")
                        put("uri", clip.image.uri)
                        put("mimeType", clip.image.mimeType)
                    }
                }
            })
        }
        preferences.edit()
            .putString(key, array.toString())
            .apply()
    }

    private fun writeLegacyMirrors(recent: List<ClipboardClip>, pinned: List<ClipboardClip>) {
        writeList("recent", recent.mapNotNull { (it as? ClipboardClip.Text)?.value })
        writePinned(pinned.mapNotNull { (it as? ClipboardClip.Text)?.value })
        writeImages(recent.mapNotNull { (it as? ClipboardClip.Image)?.image })
    }

    private fun readRecentImagesLegacy(): List<ClipboardImage> {
        val raw = preferences.getString("recent_images", "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.optJSONObject(index)
                ClipboardImage(
                    uri = item?.optString("uri").orEmpty(),
                    mimeType = item?.optString("mimeType").orEmpty()
                )
            }.filter { it.uri.isNotBlank() && it.mimeType.startsWith("image/") }
                .distinctBy { it.uri }
        }.getOrDefault(emptyList())
    }

    private fun writeImages(values: List<ClipboardImage>) {
        val array = JSONArray()
        values.forEach { image ->
            array.put(JSONObject().apply {
                put("uri", image.uri)
                put("mimeType", image.mimeType)
            })
        }
        preferences.edit()
            .putString("recent_images", array.toString())
            .apply()
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

    private companion object {
        const val MAX_CLIPS = 24
    }
}
