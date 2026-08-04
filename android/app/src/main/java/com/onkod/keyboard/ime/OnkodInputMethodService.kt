package com.onkod.keyboard.ime

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputContentInfo
import com.onkod.keyboard.SettingsActivity

class OnkodInputMethodService : InputMethodService(), OnkodKeyboardView.Listener {
    private lateinit var settingsStore: SettingsStore
    private lateinit var clipboardStore: ClipboardStore
    private lateinit var keyboardView: OnkodKeyboardView
    private var settings = KeyboardSettings()
    private var shiftState = ShiftState.LOWERCASE
    private var symbolsVisible = false
    private var symbolsPage = 1
    private var emojiVisible = false
    private var activeEmojiCategory = EmojiCategory.FACES
    private val recentEmojis = mutableListOf<String>()
    private var clipboardSelectionMode = ClipboardSelectionMode.NONE
    private val selectedClipboardClips = linkedSetOf<String>()
    private var oneHandedSide = OneHandedSide.NONE
    private var morePanelVisible = false
    private var oneHandedChooserVisible = false
    private var clipboardSuggestion: ClipboardSuggestion? = null
    private var currentClipboardId: String? = null
    private var activePackageName: String? = null
    private var clipboardListenerRegistered = false
    private val clipboardHandler = Handler(Looper.getMainLooper())
    private val dismissClipboardSuggestionRunnable = Runnable {
        consumeClipboardSuggestion(renderNow = false)
        logClipboardDebug("suggestion timeout")
        if (::keyboardView.isInitialized) render()
    }
    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        handleClipboardChanged()
    }
    private val backspaceRepeater = BackspaceRepeater { deleteOne() }

    override fun onCreate() {
        super.onCreate()
        settingsStore = SettingsStore(this)
        clipboardStore = ClipboardStore(this)
    }

    override fun onDestroy() {
        unregisterClipboardListener()
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        settings = settingsStore.read()
        registerClipboardListener()
        inspectClipboardForSuggestion()
        keyboardView = OnkodKeyboardView(this)
        keyboardView.listener = this
        render()
        return keyboardView
    }

    override fun onEvaluateInputViewShown(): Boolean = true

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        settings = settingsStore.read()
        registerClipboardListener()
        if (activePackageName != null && activePackageName != info?.packageName) {
            consumeClipboardSuggestion(renderNow = false)
        }
        activePackageName = info?.packageName
        inspectClipboardForSuggestion()
        shiftState = ShiftState.LOWERCASE
        symbolsVisible = false
        emojiVisible = false
        morePanelVisible = false
        oneHandedChooserVisible = false
        if (::keyboardView.isInitialized) render()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        registerClipboardListener()
        inspectClipboardForSuggestion()
        if (::keyboardView.isInitialized) render()
    }

    override fun onWindowHidden() {
        consumeClipboardSuggestion(renderNow = false)
        unregisterClipboardListener()
        super.onWindowHidden()
    }

    override fun onKey(action: KeyAction) {
        if (clipboardSuggestion != null) {
            consumeClipboardSuggestion(renderNow = false)
        }
        when (action) {
            is KeyAction.Text -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                commitText(outputFor(action.value, shiftState))
                if (emojiVisible) rememberEmoji(action.value)
            }
            is KeyAction.PasteText -> {
                logClipboardDebug("paste clicked text")
                val success = commitRawText(action.value)
                logClipboardDebug("commit result text=$success")
                dismissClipboardSuggestion()
            }
            is KeyAction.InsertClipboardImage -> {
                commitClipboardImage(action.image)
            }
            is KeyAction.PinClipboardClip -> {
                clipboardStore.pinClip(action.clip)
                showClipboard()
            }
            is KeyAction.UnpinClipboardClip -> {
                clipboardStore.unpinClip(action.clip)
                showClipboard()
            }
            is KeyAction.ToggleClipboardSelection -> {
                if (selectedClipboardClips.contains(action.id)) {
                    selectedClipboardClips.remove(action.id)
                } else {
                    selectedClipboardClips.add(action.id)
                }
                showClipboard(readSystemClipboard = false)
            }
            KeyAction.ClearClipboardRecent -> {
                clipboardStore.clearRecent()
                showClipboard()
            }
            KeyAction.StartClipboardPinSelection -> {
                clipboardSelectionMode = ClipboardSelectionMode.PIN
                selectedClipboardClips.clear()
                selectedClipboardClips.addAll(clipboardStore.readPinnedClips().map { it.id })
                showClipboard(readSystemClipboard = false)
            }
            KeyAction.StartClipboardDeleteSelection -> {
                clipboardSelectionMode = ClipboardSelectionMode.DELETE
                selectedClipboardClips.clear()
                showClipboard(readSystemClipboard = false)
            }
            KeyAction.SelectAllClipboard -> {
                val allClips = (clipboardStore.readRecentClips() + clipboardStore.readPinnedClips()).distinctBy { it.id }
                if (selectedClipboardClips.size == allClips.size) {
                    selectedClipboardClips.clear()
                } else {
                    selectedClipboardClips.clear()
                    selectedClipboardClips.addAll(allClips.map { it.id })
                }
                showClipboard(readSystemClipboard = false)
            }
            KeyAction.ConfirmClipboardPinSelection -> {
                val allClips = (clipboardStore.readRecentClips() + clipboardStore.readPinnedClips()).distinctBy { it.id }
                clipboardStore.replacePinnedClips(allClips.filter { selectedClipboardClips.contains(it.id) })
                clipboardSelectionMode = ClipboardSelectionMode.NONE
                selectedClipboardClips.clear()
                showClipboard(readSystemClipboard = false)
            }
            KeyAction.ConfirmClipboardDeleteSelection -> {
                clipboardStore.deleteClips(selectedClipboardClips)
                clipboardSelectionMode = ClipboardSelectionMode.NONE
                selectedClipboardClips.clear()
                showClipboard(readSystemClipboard = false)
            }
            KeyAction.ExitClipboardSelection -> {
                clipboardSelectionMode = ClipboardSelectionMode.NONE
                selectedClipboardClips.clear()
                showClipboard(readSystemClipboard = false)
            }
            is KeyAction.EmojiCategorySelect -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                activeEmojiCategory = action.category
                emojiVisible = true
                symbolsVisible = false
                render()
            }
            is KeyAction.SymbolsPage -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                symbolsPage = action.page
                symbolsVisible = true
                emojiVisible = false
                render()
            }
            KeyAction.Space -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                commitText(" ")
            }
            KeyAction.Period -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                commitText(".")
            }
            KeyAction.Enter -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                performEnter()
            }
            KeyAction.Shift -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                toggleShift()
            }
            KeyAction.Backspace -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                deleteOne()
            }
            KeyAction.Symbols -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                symbolsVisible = true
                symbolsPage = 1
                emojiVisible = false
                render()
            }
            KeyAction.Abc -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                symbolsVisible = false
                emojiVisible = false
                render()
            }
            KeyAction.Globe -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                switchInternalLanguage()
            }
            KeyAction.HideKeyboard -> requestHideSelf(0)
            KeyAction.EmojiPanel -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                emojiVisible = true
                symbolsVisible = false
                activeEmojiCategory = EmojiCategory.FACES
                render()
            }
            KeyAction.Settings -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                openSettings()
            }
            KeyAction.Clipboard -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                showClipboard()
            }
            KeyAction.DismissClipboardSuggestion -> dismissClipboardSuggestion()
            KeyAction.OneHandedKeyboard -> {
                oneHandedChooserVisible = true
                keyboardView.showOneHandedChooser(KeyboardLayouts.forMode(settings.activeMode()).toolbar, oneHandedSide)
            }
            is KeyAction.SetOneHandedSide -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                oneHandedSide = action.side
                symbolsVisible = false
                emojiVisible = false
                render()
            }
            KeyAction.ExitOneHandedKeyboard -> {
                morePanelVisible = false
                oneHandedChooserVisible = false
                oneHandedSide = OneHandedSide.NONE
                render()
            }
            KeyAction.More -> {
                morePanelVisible = !morePanelVisible
                oneHandedChooserVisible = false
                symbolsVisible = false
                emojiVisible = false
                if (morePanelVisible) {
                    keyboardView.showMorePanel(KeyboardLayouts.forMode(settings.activeMode()).toolbar)
                } else {
                    render()
                }
            }
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

    private fun commitRawText(value: String): Boolean =
        currentInputConnection?.commitText(value, 1) == true

    private fun showClipboard(readSystemClipboard: Boolean = true) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val image = if (readSystemClipboard) readClipboardImage(clipboard) else null
        if (image != null) {
            clipboardStore.rememberImage(image)
        }
        val text = if (readSystemClipboard && image == null) {
            clipboard.primaryClip
                ?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)
                ?.coerceToText(this)
                ?.toString()
                ?.takeIf { it.isNotBlank() }
        } else null
        if (text != null) clipboardStore.rememberRecent(text)
        val unreadableImageIds = linkedSetOf<String>()
        val pinned = clipboardStore.readPinnedClips().filterReadableClipboardImages(unreadableImageIds)
        val pinnedIds = pinned.map { it.id }.toSet()
        val recent = clipboardStore.readRecentClips()
            .filterReadableClipboardImages(unreadableImageIds)
            .filterNot { pinnedIds.contains(it.id) }
        if (unreadableImageIds.isNotEmpty()) {
            clipboardStore.deleteClips(unreadableImageIds)
            logClipboardDebug("removed unreadable image clips count=${unreadableImageIds.size}")
        }
        keyboardView.showClipboardPanel(
            currentText = text,
            recentClips = recent,
            pinnedClips = pinned,
            selectionMode = clipboardSelectionMode,
            selectedClips = selectedClipboardClips
        )
    }

    private fun deleteOne() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun performEnter() {
        val connection = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo
        val actionId = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        val inputType = editorInfo?.inputType ?: 0
        val isMultiline = inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0
        val classType = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val isPassword = classType == InputType.TYPE_CLASS_TEXT && variation in setOf(
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        ) || classType == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
        val hasEditorAction = actionId != EditorInfo.IME_ACTION_NONE &&
            actionId != EditorInfo.IME_ACTION_UNSPECIFIED

        when {
            hasEditorAction -> connection.performEditorAction(actionId)
            isMultiline && !isPassword -> connection.commitText("\n", 1)
            else -> {
                connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
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
        morePanelVisible = false
        oneHandedChooserVisible = false
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
            recentEmojis = recentEmojis,
            oneHandedSide = oneHandedSide,
            clipboardSuggestion = clipboardSuggestion
        )
    }

    private fun registerClipboardListener() {
        if (clipboardListenerRegistered) return
        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).addPrimaryClipChangedListener(clipboardListener)
        clipboardListenerRegistered = true
    }

    private fun unregisterClipboardListener() {
        if (!clipboardListenerRegistered) return
        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).removePrimaryClipChangedListener(clipboardListener)
        clipboardListenerRegistered = false
    }

    private fun List<ClipboardClip>.filterReadableClipboardImages(unreadableIds: MutableSet<String>): List<ClipboardClip> =
        filter { clip ->
            when (clip) {
                is ClipboardClip.Text -> true
                is ClipboardClip.Image -> {
                    val readable = canOpenImageUri(clip.image.uri)
                    if (!readable) unreadableIds.add(clip.id)
                    readable
                }
            }
        }

    private fun canOpenImageUri(uriString: String): Boolean =
        runCatching {
            contentResolver.openInputStream(Uri.parse(uriString))?.use { true } ?: false
        }.getOrDefault(false)

    private fun handleClipboardChanged() {
        logClipboardDebug("clipboard changed")
        inspectClipboardForSuggestion()
        if (::keyboardView.isInitialized) render()
    }

    private fun inspectClipboardForSuggestion() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val suggestion = readClipboardSuggestion(clipboard)
        clipboardSuggestion = suggestion
        currentClipboardId = suggestion?.clipboardId()
        when (suggestion) {
            is ClipboardSuggestion.Image -> {
                clipboardStore.rememberImage(suggestion.image)
                clipboardStore.rememberAutomaticallySuggestedClipboardId(suggestion.id)
                logClipboardDebug("image detected mime=${suggestion.image.mimeType}")
                logClipboardDebug("suggestion shown image")
            }
            is ClipboardSuggestion.Text -> {
                clipboardStore.rememberRecent(suggestion.value)
                clipboardStore.rememberAutomaticallySuggestedClipboardId(suggestion.id)
                logClipboardDebug("text detected")
                logClipboardDebug("suggestion shown text")
            }
            null -> logClipboardDebug("no supported clipboard item")
        }
        clipboardHandler.removeCallbacks(dismissClipboardSuggestionRunnable)
        if (suggestion != null) {
            clipboardHandler.postDelayed(dismissClipboardSuggestionRunnable, CLIPBOARD_SUGGESTION_TIMEOUT_MS)
        }
    }

    private fun dismissClipboardSuggestion() {
        consumeClipboardSuggestion(renderNow = false)
        currentClipboardId = null
        clipboardHandler.removeCallbacks(dismissClipboardSuggestionRunnable)
        if (::keyboardView.isInitialized) render()
    }

    private fun readClipboardSuggestion(clipboard: ClipboardManager): ClipboardSuggestion? {
        val clip = runCatching { clipboard.primaryClip }.getOrNull() ?: return null
        val description = clip.description
        if (description != null) logClipboardDebug("mime=${clipMimeTypes(description)}")
        readClipboardImage(clipboard)?.let { image ->
            val id = ClipboardIdentity.image(
                mimeTypes = clipMimeTypes(description),
                mimeType = image.mimeType,
                uri = image.uri,
                timestamp = null
            )
            if (isClipboardIdentityAlreadyUsed(id)) return null
            return ClipboardSuggestion.Image(image, id)
        }
        val sensitive = description?.isSensitiveClipboardContent() == true
        for (index in 0 until clip.itemCount) {
            val text = clip.getItemAt(index).text?.toString()?.takeIf { it.isNotBlank() } ?: continue
            val id = ClipboardIdentity.text(
                mimeTypes = clipMimeTypes(description),
                value = text,
                timestamp = description?.safeTimestamp()
            )
            if (isClipboardIdentityAlreadyUsed(id)) return null
            return ClipboardSuggestion.Text(text, sensitive, id)
        }
        return null
    }

    private fun isClipboardIdentityAlreadyUsed(id: String): Boolean =
        (currentClipboardId != id && clipboardStore.readLastAutomaticallySuggestedClipboardId() == id) ||
            clipboardStore.readLastConsumedClipboardId() == id

    private fun consumeClipboardSuggestion(renderNow: Boolean = true) {
        val id = clipboardSuggestion?.clipboardId() ?: currentClipboardId
        if (id != null) clipboardStore.rememberConsumedClipboardId(id)
        clipboardSuggestion = null
        currentClipboardId = null
        clipboardHandler.removeCallbacks(dismissClipboardSuggestionRunnable)
        if (renderNow && ::keyboardView.isInitialized) render()
    }

    private fun ClipboardSuggestion.clipboardId(): String = when (this) {
        is ClipboardSuggestion.Text -> id
        is ClipboardSuggestion.Image -> id
    }

    private fun ClipDescription.isSensitiveClipboardContent(): Boolean {
        val extras = extras ?: return false
        return extras.getBoolean("android.content.extra.IS_SENSITIVE", false) ||
            extras.getBoolean("androidx.core.content.extra.IS_SENSITIVE", false)
    }

    private fun ClipDescription.safeTimestamp(): Long =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) timestamp else 0L

    private fun readClipboardImage(clipboard: ClipboardManager): ClipboardImage? {
        val clip = runCatching { clipboard.primaryClip }.getOrNull() ?: return null
        val description = clip.description ?: return null
        for (index in 0 until clip.itemCount) {
            val uri = clip.getItemAt(index).uri ?: continue
            val mimeType = imageMimeTypeFor(uri, description) ?: continue
            return ClipboardImage(uri = uri.toString(), mimeType = mimeType)
        }
        return null
    }

    private fun imageMimeTypeFor(uri: Uri, description: ClipDescription): String? {
        runCatching { contentResolver.getType(uri) }.getOrNull()
            ?.takeIf { it.startsWith("image/") && it != "image/*" }
            ?.let { return normalizeImageMimeType(it) }
        for (index in 0 until description.mimeTypeCount) {
            val mimeType = description.getMimeType(index)
            if (mimeType.startsWith("image/") && mimeType != "image/*") return normalizeImageMimeType(mimeType)
        }
        inferImageMimeTypeFromUri(uri)?.let { return it }
        return null
    }

    private fun inferImageMimeTypeFromUri(uri: Uri): String? = when {
        uri.toString().substringBefore('?').lowercase().endsWith(".png") -> "image/png"
        uri.toString().substringBefore('?').lowercase().endsWith(".jpg") -> "image/jpeg"
        uri.toString().substringBefore('?').lowercase().endsWith(".jpeg") -> "image/jpeg"
        uri.toString().substringBefore('?').lowercase().endsWith(".webp") -> "image/webp"
        else -> null
    }

    private fun normalizeImageMimeType(mimeType: String): String = when (mimeType.lowercase()) {
        "image/jpg" -> "image/jpeg"
        else -> mimeType.lowercase()
    }

    private fun commitClipboardImage(image: ClipboardImage) {
        logClipboardDebug("paste clicked image")
        val editorInfo = currentInputEditorInfo
        val connection = currentInputConnection
        if (editorInfo == null || connection == null) {
            keyboardView.showMessagePanel("This chat does not accept images from the keyboard.")
            logClipboardDebug("commit result image=false")
            return
        }
        val supportedMimeTypes = editorInfo.contentMimeTypes.orEmpty()
        val acceptsImage = supportedMimeTypes.any { supportedMimeType ->
            mimeMatches(supportedMimeType, image.mimeType)
        }
        logImageCommitAttempt(editorInfo.packageName, supportedMimeTypes, image.mimeType, null)
        if (!acceptsImage) {
            keyboardView.showMessagePanel("This chat does not accept images from the keyboard.")
            logImageCommitAttempt(editorInfo.packageName, supportedMimeTypes, image.mimeType, false)
            logClipboardDebug("commit result image=false")
            return
        }

        val uri = Uri.parse(image.uri)
        val contentInfo = InputContentInfo(
            uri,
            ClipDescription("Onkod image", arrayOf(image.mimeType)),
            null
        )
        runCatching {
            grantUriPermission(editorInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val success = connection.commitContent(
            contentInfo,
            InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION,
            null
        )
        logImageCommitAttempt(editorInfo.packageName, supportedMimeTypes, image.mimeType, success)
        logClipboardDebug("commit result image=$success")
        if (success) {
            dismissClipboardSuggestion()
        } else {
            keyboardView.showMessagePanel("This chat does not accept images from the keyboard.")
        }
    }

    private fun mimeMatches(supportedMimeType: String, attemptedMimeType: String): Boolean {
        if (supportedMimeType == attemptedMimeType) return true
        if (supportedMimeType == "image/*" && attemptedMimeType.startsWith("image/")) return true
        val slash = supportedMimeType.indexOf('/')
        return slash > 0 &&
            supportedMimeType.substring(slash + 1) == "*" &&
            attemptedMimeType.startsWith("${supportedMimeType.substring(0, slash)}/")
    }

    private fun logImageCommitAttempt(
        packageName: String?,
        supportedMimeTypes: Array<out String>,
        attemptedMimeType: String,
        success: Boolean?
    ) {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return
        Log.d(
            "OnkodImageClipboard",
            "package=$packageName supported=${supportedMimeTypes.joinToString()} attempted=$attemptedMimeType success=${success ?: "pending"}"
        )
    }

    private fun clipMimeTypes(description: ClipDescription): String =
        (0 until description.mimeTypeCount).joinToString { index -> description.getMimeType(index) }

    private fun logClipboardDebug(message: String) {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return
        Log.d("OnkodClipboard", message)
    }

    private fun rememberEmoji(value: String) {
        recentEmojis.remove(value)
        recentEmojis.add(0, value)
        if (recentEmojis.size > 48) {
            recentEmojis.removeAt(recentEmojis.lastIndex)
        }
    }

    private companion object {
        const val CLIPBOARD_SUGGESTION_TIMEOUT_MS = 12_000L
    }
}
