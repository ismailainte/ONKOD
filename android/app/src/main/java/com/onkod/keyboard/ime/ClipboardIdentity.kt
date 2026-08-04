package com.onkod.keyboard.ime

import java.security.MessageDigest

object ClipboardIdentity {
    fun text(mimeTypes: String, value: String, timestamp: Long?): String =
        build(kind = "text", mimeTypes = mimeTypes, payload = value, timestamp = timestamp)

    fun image(mimeTypes: String, mimeType: String, uri: String, timestamp: Long?): String =
        build(kind = "image", mimeTypes = mimeTypes, payload = "$mimeType|$uri", timestamp = timestamp)

    private fun build(kind: String, mimeTypes: String, payload: String, timestamp: Long?): String {
        val base = "$kind|$mimeTypes|${timestamp ?: 0L}|$payload"
        return "$kind:${base.sha256()}"
    }

    private fun String.sha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }
}
