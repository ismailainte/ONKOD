package com.somalikeyboard.app.keyboard

import android.inputmethodservice.InputMethodService
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Abstracts over "the field currently being typed into" so [KeyboardController] can drive
 * either a real app's [android.view.inputmethod.InputConnection] (the actual IME) or a local
 * Compose text field (the in-app preview screen) with identical logic.
 */
interface TextTarget {
    val length: Int
    fun insert(text: String)
    fun backspace()

    /** Whole current field contents, used by the "save current draft as a clip" feature. */
    fun currentText(): String
}

private const val SURROUNDING_TEXT_LIMIT = 4000

class InputConnectionTextTarget(private val service: InputMethodService) : TextTarget {
    override var length: Int = 0
        private set

    init {
        refreshCount()
    }

    fun refreshCount() {
        val ic = service.currentInputConnection
        if (ic == null) {
            length = 0
            return
        }
        val before = ic.getTextBeforeCursor(SURROUNDING_TEXT_LIMIT, 0)?.length ?: 0
        val after = ic.getTextAfterCursor(SURROUNDING_TEXT_LIMIT, 0)?.length ?: 0
        length = before + after
    }

    override fun insert(text: String) {
        service.currentInputConnection?.commitText(text, 1)
        refreshCount()
    }

    override fun backspace() {
        val ic = service.currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
        refreshCount()
    }

    override fun currentText(): String {
        val ic = service.currentInputConnection ?: return ""
        val before = ic.getTextBeforeCursor(SURROUNDING_TEXT_LIMIT, 0)?.toString().orEmpty()
        val after = ic.getTextAfterCursor(SURROUNDING_TEXT_LIMIT, 0)?.toString().orEmpty()
        return before + after
    }
}

/** Backs the live keyboard preview embedded in [com.somalikeyboard.app.MainActivity]. */
class LocalTextTarget(private val fieldState: MutableState<TextFieldValue>) : TextTarget {
    override val length: Int
        get() = fieldState.value.text.length

    override fun insert(text: String) {
        val cur = fieldState.value
        val start = cur.selection.start.coerceIn(0, cur.text.length)
        val end = cur.selection.end.coerceIn(0, cur.text.length)
        val newText = cur.text.substring(0, start) + text + cur.text.substring(end)
        fieldState.value = TextFieldValue(newText, TextRange(start + text.length))
    }

    override fun backspace() {
        val cur = fieldState.value
        val start = cur.selection.start.coerceIn(0, cur.text.length)
        val end = cur.selection.end.coerceIn(0, cur.text.length)
        if (start != end) {
            val newText = cur.text.removeRange(start, end)
            fieldState.value = TextFieldValue(newText, TextRange(start))
        } else if (start > 0) {
            val newText = cur.text.removeRange(start - 1, start)
            fieldState.value = TextFieldValue(newText, TextRange(start - 1))
        }
    }

    override fun currentText(): String = fieldState.value.text
}
