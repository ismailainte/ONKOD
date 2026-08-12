package com.somalikeyboard.app.ime

import android.content.SharedPreferences
import android.content.res.Configuration
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import com.somalikeyboard.app.keyboard.EnterMode
import com.somalikeyboard.app.keyboard.InputConnectionTextTarget
import com.somalikeyboard.app.keyboard.KeyboardController
import com.somalikeyboard.app.keyboard.KeyboardState
import com.somalikeyboard.app.keyboard.Page
import com.somalikeyboard.app.keyboard.ShiftState
import com.somalikeyboard.app.keyboard.audio.FeedbackPlayer
import com.somalikeyboard.app.keyboard.ui.KeyboardScreen
import com.somalikeyboard.app.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * The real, system-wide Somali keyboard. Renders the same [KeyboardScreen] used by the
 * in-app preview, but backed by the focused app's [android.view.inputmethod.InputConnection]
 * instead of a local text field.
 */
class SomaliKeyboardService : LifecycleInputMethodService() {

    private val state = KeyboardState()
    private lateinit var settings: SettingsStore
    private lateinit var feedback: FeedbackPlayer
    private lateinit var target: InputConnectionTextTarget
    private lateinit var controller: KeyboardController
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        syncSettings()
    }

    override fun onCreate() {
        super.onCreate()
        settings = SettingsStore(this)
        feedback = FeedbackPlayer(this)
        target = InputConnectionTextTarget(this)
        controller = KeyboardController(state, target, feedback, serviceScope)
        syncSettings()
        settings.registerListener(prefsListener)
        applySystemDarkMode(resources.configuration)
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)
        composeView.attachComposeOwners()
        composeView.setContent {
            KeyboardScreen(state = state, controller = controller)
        }
        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        state.page = Page.ABC
        state.shift = ShiftState.NONE
        state.toast = ""
        state.defaultEnterMode = enterModeFor(info)
        target.refreshCount()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySystemDarkMode(newConfig)
    }

    override fun onDestroy() {
        settings.unregisterListener(prefsListener)
        feedback.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun syncSettings() {
        state.haptics = settings.haptics
        state.sound = settings.sound
    }

    private fun applySystemDarkMode(config: Configuration) {
        val nightMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        state.systemDark = nightMode == Configuration.UI_MODE_NIGHT_YES
    }

    /** A real keyboard should show a search glyph when the focused field asked for one. */
    private fun enterModeFor(info: EditorInfo?): EnterMode {
        val action = (info?.imeOptions ?: EditorInfo.IME_ACTION_UNSPECIFIED) and EditorInfo.IME_MASK_ACTION
        return if (action == EditorInfo.IME_ACTION_SEARCH) EnterMode.SEARCH else EnterMode.ENTER
    }
}
