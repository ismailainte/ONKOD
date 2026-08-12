package com.somalikeyboard.app.keyboard

import com.somalikeyboard.app.keyboard.audio.FeedbackPlayer
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val ENGLISH_DIGRAPH_MAP = mapOf("sh" to "z", "dh" to "v", "kh" to "p")
private const val TOAST_DURATION_MS = 1400L
private const val SPACE_LONG_PRESS_MS = 500L
private const val SPACE_SWIPE_THRESHOLD_PX = 26f
private const val BACKSPACE_REPEAT_DELAY_MS = 380L
private const val BACKSPACE_REPEAT_INTERVAL_MS = 55L

/**
 * Port of the `press`/`insert`/`backspace`/`toggleLayout`/`saveClip`/`openClipboard` logic from
 * the `Component` class in the original design (Somali Keyboard.dc.html), operating against a
 * [TextTarget] instead of a DOM textarea.
 */
class KeyboardController(
    private val state: KeyboardState,
    private val target: TextTarget,
    private val feedback: FeedbackPlayer,
    private val scope: CoroutineScope,
    /** Density-independent swipe threshold in raw pixels for the current display. */
    private val swipeThresholdPx: Float = SPACE_SWIPE_THRESHOLD_PX,
) {
    private var toastJob: Job? = null
    private var holdJob: Job? = null
    private var repeatJob: Job? = null

    private var spaceDownX: Float? = null
    private var spaceLongPressed = false

    fun onKeyDown(id: String, x: Float) {
        feedback.trigger(state.haptics, state.sound)

        if (id == "space") {
            spaceDownX = x
            spaceLongPressed = false
            holdJob?.cancel()
            holdJob = scope.launch {
                delay(SPACE_LONG_PRESS_MS)
                spaceLongPressed = true
                toggleLayout()
            }
            return
        }

        press(id)

        if (id == "bs") {
            repeatJob?.cancel()
            repeatJob = scope.launch {
                delay(BACKSPACE_REPEAT_DELAY_MS)
                while (isActive) {
                    feedback.trigger(state.haptics, state.sound)
                    backspace()
                    delay(BACKSPACE_REPEAT_INTERVAL_MS)
                }
            }
        }
    }

    fun onKeyUp(id: String, x: Float) {
        stopRepeat()
        val startX = spaceDownX
        if (startX != null) {
            spaceDownX = null
            if (!spaceLongPressed) {
                val dx = x - startX
                if (abs(dx) > swipeThresholdPx) {
                    toggleLayout()
                } else {
                    insert(" ")
                    if (state.shift == ShiftState.SINGLE) state.shift = ShiftState.NONE
                }
            }
        }
    }

    fun onKeyCancel() {
        stopRepeat()
        spaceDownX = null
    }

    private fun stopRepeat() {
        holdJob?.cancel(); holdJob = null
        repeatJob?.cancel(); repeatJob = null
    }

    // Mirrors Component.press(k) — note several branches `return` before reaching the
    // shift-reset at the bottom, so shift only auto-clears after space/enter/a letter.
    private fun press(k: String) {
        when {
            k == "emoji" -> {
                state.page = if (state.page == Page.EMOJI) Page.ABC else Page.EMOJI
                return
            }
            k == "clipboard" -> {
                if (state.page == Page.CLIP) state.page = Page.ABC else openClipboard()
                return
            }
            k.startsWith("clip:") -> {
                val id = k.removePrefix("clip:")
                state.clips.firstOrNull { it.id == id }?.let { insert(it.text) }
                return
            }
            k.startsWith("pin:") -> {
                val id = k.removePrefix("pin:")
                val idx = state.clips.indexOfFirst { it.id == id }
                if (idx >= 0) {
                    val c = state.clips[idx]
                    state.clips[idx] = c.copy(pinned = !c.pinned)
                }
                return
            }
            k == "clipsave" -> {
                saveClip()
                return
            }
            k == "clipclear" -> {
                val kept = state.clips.filter { it.pinned }
                state.clips.clear()
                state.clips.addAll(kept)
                showToast("Recent clips cleared")
                return
            }
            k.startsWith("cat:") -> {
                state.category = EmojiCategory.fromId(k.removePrefix("cat:"))
                return
            }
            k == "shift" -> {
                state.shift = when (state.shift) {
                    ShiftState.NONE -> ShiftState.SINGLE
                    ShiftState.SINGLE -> ShiftState.CAPS
                    ShiftState.CAPS -> ShiftState.NONE
                }
                return
            }
            k == "sym" -> { state.page = Page.SYM1; return }
            k == "abc" -> { state.page = Page.ABC; return }
            k == "page2" -> { state.page = Page.SYM2; return }
            k == "page1" -> { state.page = Page.SYM1; return }
            k == "globe" -> {
                state.lang = if (state.lang == Lang.SO) Lang.EN else Lang.SO
                showToast(if (state.lang == Lang.SO) "Soomaali (SO)" else "English (US)")
                return
            }
            k == "tool" -> { showToast("Toolbar shortcut"); return }
            // Decorative search field in the emoji panel — not wired up in the source design.
            k == "search" -> return
            k == "bs" -> { backspace(); return }
        }

        when (k) {
            "space" -> insert(" ")
            "enter" -> insert("\n")
            else -> {
                val mapped = ENGLISH_DIGRAPH_MAP[k]
                val out = if (state.lang == Lang.EN && mapped != null) mapped else k
                val upper = state.page == Page.ABC && state.shift != ShiftState.NONE
                insert(if (upper) out.uppercase() else out)
            }
        }

        if (state.shift == ShiftState.SINGLE) state.shift = ShiftState.NONE
    }

    private fun insert(text: String) {
        target.insert(text)
        state.charCount = target.length
    }

    private fun backspace() {
        target.backspace()
        state.charCount = target.length
    }

    private fun toggleLayout() {
        val order = listOf(KeyboardLayout.QWERTY, KeyboardLayout.AZERTY, KeyboardLayout.BTJ)
        val next = order[(order.indexOf(state.layout) + 1) % order.size]
        state.layout = next
        showToast(next.name)
    }

    /** Returns true if a new clip was actually appended. */
    private fun saveClip(): Boolean {
        val text = target.currentText().trim()
        if (text.isEmpty()) {
            showToast("Nothing to save")
            return false
        }
        if (state.clips.any { it.text == text }) return false
        state.clips.add(0, ClipItem("c" + System.currentTimeMillis(), text))
        return true
    }

    private fun openClipboard() {
        val saved = saveClip()
        state.page = Page.CLIP
        if (saved) showToast("Saved to clipboard")
    }

    private fun showToast(msg: String) {
        state.toast = msg
        toastJob?.cancel()
        toastJob = scope.launch {
            delay(TOAST_DURATION_MS)
            state.toast = ""
        }
    }

    fun toggleTheme() {
        state.themeOverride = if (state.resolvedTheme == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
    }

    fun toggleHaptics() { state.haptics = !state.haptics }

    fun toggleSound() { state.sound = !state.sound }

    fun setEnterModeOverride(mode: EnterMode) { state.enterModeOverride = mode }
}
