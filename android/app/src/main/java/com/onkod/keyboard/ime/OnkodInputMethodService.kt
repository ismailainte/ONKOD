package com.onkod.keyboard.ime

import android.content.ClipboardManager
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
    private var symbolsPage = 1
    private var emojiVisible = false
    private var activeEmojiCategory = EmojiCategory.FACES
    private val recentEmojis = mutableListOf<String>()
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

    override fun onEvaluateInputViewShown(): Boolean = true

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
            is KeyAction.Text -> {
                commitText(outputFor(action.value, shiftState))
                if (emojiVisible) rememberEmoji(action.value)
            }
            is KeyAction.PasteText -> {
                commitRawText(action.value)
                symbolsVisible = false
                emojiVisible = false
                render()
            }
            is KeyAction.EmojiCategorySelect -> {
                activeEmojiCategory = action.category
                emojiVisible = true
                symbolsVisible = false
                render()
            }
            is KeyAction.SymbolsPage -> {
                symbolsPage = action.page
                symbolsVisible = true
                emojiVisible = false
                render()
            }
            KeyAction.Space -> commitText(" ")
            KeyAction.Period -> commitText(".")
            KeyAction.Enter -> performEnter()
            KeyAction.Shift -> toggleShift()
            KeyAction.Backspace -> deleteOne()
            KeyAction.Symbols -> {
                symbolsVisible = true
                symbolsPage = 1
                emojiVisible = false
                render()
            }
            KeyAction.Abc -> {
                symbolsVisible = false
                emojiVisible = false
                render()
            }
            KeyAction.Globe -> switchInternalLanguage()
            KeyAction.HideKeyboard -> requestHideSelf(0)
            KeyAction.EmojiPanel -> {
                emojiVisible = true
                symbolsVisible = false
                activeEmojiCategory = EmojiCategory.FACES
                render()
            }
            KeyAction.Settings -> openSettings()
            KeyAction.Clipboard -> showClipboard()
            KeyAction.More -> keyboardView.showMessagePanel("Theme, layout, privacy, and about shortcuts are available in the Onkod app.")
        }
    }

    override fun onBackspaceHoldStart() {
        backspaceRepeater.start(settings.longPressDelay)
    }

    override fun onBackspaceHoldEnd() {
        backspaceRepeater.stop()
    }

    override fun onGlobeLongPress() {
        showInputMethodPicker()
    }

    private fun commitText(value: String) {
        currentInputConnection?.commitText(value, 1)
        if (shiftState == ShiftState.SHIFT) {
            shiftState = ShiftState.LOWERCASE
            render()
        }
    }

    private fun commitRawText(value: String) {
        currentInputConnection?.commitText(value, 1)
    }

    private fun showClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            ?.takeIf { it.isNotBlank() }
        keyboardView.showClipboardPanel(text)
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

    private fun switchInternalLanguage() {
        settings = settingsStore.write(settings.nextInternalLanguage())
        symbolsVisible = false
        emojiVisible = false
        shiftState = ShiftState.LOWERCASE
        render()
    }

    private fun showInputMethodPicker() {
        val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.showInputMethodPicker()
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("screen", "settings")
        startActivity(intent)
    }

    private fun render() {
        val layout = KeyboardLayouts.forMode(settings.activeMode())
        keyboardView.render(
            layout = layout,
            settings = settings,
            shiftState = shiftState,
            symbolsVisible = symbolsVisible,
            symbolsPage = symbolsPage,
            emojiVisible = emojiVisible,
            activeEmojiCategory = activeEmojiCategory,
            recentEmojis = recentEmojis
        )
    }

    private fun rememberEmoji(value: String) {
        recentEmojis.remove(value)
        recentEmojis.add(0, value)
        if (recentEmojis.size > 48) {
            recentEmojis.removeAt(recentEmojis.lastIndex)
        }
    }
}
