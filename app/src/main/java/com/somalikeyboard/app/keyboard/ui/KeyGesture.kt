package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.somalikeyboard.app.keyboard.KeyboardController

/**
 * Shared down/move/up gesture used by every pressable surface in the keyboard (letter keys,
 * emoji, toolbar icons, category tabs, clip cards). Reports the raw x position on down/up so
 * [KeyboardController] can implement the spacebar swipe-to-switch-layout gesture; every other
 * key ignores the x value.
 */
fun Modifier.keyPressGesture(
    id: String,
    controller: KeyboardController,
    onPressChange: (Boolean) -> Unit = {},
): Modifier = pointerInput(id) {
    awaitEachGesture {
        // requireUnconsumed (default true) matters for nested pressables like the clipboard
        // cards: the inner "pin"/"unpin" label must consume the down event first so the
        // outer card's own gesture (which inserts the clip) doesn't also fire for that tap.
        val down = awaitFirstDown()
        down.consume()
        onPressChange(true)
        controller.onKeyDown(id, down.position.x)
        var lastX = down.position.x
        var lost = false
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change == null) {
                lost = true
                break
            }
            lastX = change.position.x
            change.consume()
            if (!change.pressed) break
        }
        onPressChange(false)
        if (lost) controller.onKeyCancel() else controller.onKeyUp(id, lastX)
    }
}
