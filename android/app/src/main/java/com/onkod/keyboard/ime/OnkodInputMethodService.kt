package com.onkod.keyboard.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.onkod.keyboard.SettingsActivity

class OnkodInputMethodService : InputMethodService(), OnkodKeyboardView.Listener {
    private lateinit var settingsStore: SettingsStore
    private lateinit var keyboardView: OnkodKeyboardView
    private var settings = KeyboardSettings()
    private var shiftState = ShiftState.LOWERCASE
    private var symbolsVisible = false
    private var emojiVisible = false
    private val backspaceRepeater = BackspaceRepeater { deleteOne() }

    override fun onCreate() {
        super.onCreate()
        settingsStore = SettingsStore(this)
    }

    override fun onCreateInputView(): View {
        settings = settingsStore.read()
        keyboardView = OnkodKeyboardView(this)
        keyboardView.listener = this
        render()
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        settings = settingsStore.read()
        shiftState = ShiftState.LOWERCASE
        symbolsVisible = false
        emojiVisible = false
        if (::keyboardView.isInitialized) render()
    }

    override fun onKey(action: KeyAction) {
        when (action) {
            is KeyAction.Text -> commitText(outputFor(action.value, shiftState))
            KeyAction.Space -> commitText(" ")
            KeyAction.Period -> commitText(".")
            KeyAction.Enter -> performEnter()
            KeyAction.Shift -> toggleShift()
            KeyAction.Backspace -> deleteOne()
            KeyAction.Symbols -> {
                symbolsVisible = true
                emojiVisible = false
                render()
            }
            KeyAction.Abc -> {
                symbolsVisible = false
                emojiVisible = false
                render()
            }
            KeyAction.Globe -> switchInputMethod()
            KeyAction.HideKeyboard -> requestHideSelf(0)
            KeyAction.EmojiPanel -> {
                emojiVisible = true
                symbolsVisible = false
                render()
            }
            KeyAction.Settings -> openSettings()
            KeyAction.Clipboard -> keyboardView.showMessagePanel("Clipboard history is not enabled in this MVP.")
            KeyAction.More -> keyboardView.showMessagePanel("Theme, layout, privacy, and about shortcuts are available in the Onkod app.")
        }
    }

    override fun onBackspaceHoldStart() {
        backspaceRepeater.start(settings.longPressDelay)
    }

    override fun onBackspaceHoldEnd() {
        backspaceRepeater.stop()
    }

    private fun commitText(value: String) {
        currentInputConnection?.commitText(value, 1)
        if (shiftState == ShiftState.SHIFT) {
            shiftState = ShiftState.LOWERCASE
            render()
        }
    }

    private fun deleteOne() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun performEnter() {
        val actionId = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (actionId != EditorInfo.IME_ACTION_NONE) {
            currentInputConnection?.performEditorAction(actionId)
        } else {
            currentInputConnection?.commitText("\n", 1)
        }
    }

    private fun toggleShift() {
        shiftState = when (shiftState) {
            ShiftState.LOWERCASE -> ShiftState.SHIFT
            ShiftState.SHIFT -> ShiftState.CAPS
            ShiftState.CAPS -> ShiftState.LOWERCASE
        }
        render()
    }

    private fun switchInputMethod() {
        val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            manager.showInputMethodPicker()
        }
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("screen", "settings")
        startActivity(intent)
    }

    private fun render() {
        val layout = KeyboardLayouts.forType(settings.layout)
        keyboardView.render(
            layout = layout,
            settings = settings,
            shiftState = shiftState,
            symbolsVisible = symbolsVisible,
            emojiVisible = emojiVisible
        )
    }
}
