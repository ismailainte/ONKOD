package com.onkod.keyboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onkod.keyboard.ime.KeyboardLayouts
import com.onkod.keyboard.ime.KeyboardMode
import com.onkod.keyboard.ime.LayoutGroup
import com.onkod.keyboard.ime.SettingsStore

class MainActivity : ComponentActivity() {
    private lateinit var store: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = SettingsStore(this)
        requestImagePermissionIfNeeded()
        setContent { OnkodApp() }
    }

    @Composable
    private fun OnkodApp() {
        var screen by remember { mutableStateOf(Screen.WELCOME) }
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (screen) {
                        Screen.WELCOME -> WelcomeScreen { screen = Screen.ENABLE }
                        Screen.ENABLE -> EnableScreen(
                            enabled = isKeyboardEnabled(),
                            onOpenSettings = { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
                            onNext = { screen = Screen.SELECT }
                        )
                        Screen.SELECT -> SelectKeyboardScreen(
                            onPicker = {
                                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                manager.showInputMethodPicker()
                            },
                            onNext = { screen = Screen.LAYOUT }
                        )
                        Screen.LAYOUT -> LayoutSelectionScreen(
                            onSettings = { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) },
                            onPreview = { screen = Screen.PREVIEW }
                        )
                        Screen.PREVIEW -> PreviewScreen(
                            onSettings = { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) },
                            onPrivacy = { screen = Screen.PRIVACY },
                            onAbout = { screen = Screen.ABOUT }
                        )
                        Screen.PRIVACY -> PrivacyScreen { screen = Screen.PREVIEW }
                        Screen.ABOUT -> AboutScreen { screen = Screen.PREVIEW }
                    }
                }
            }
        }
    }

    @Composable
    private fun WelcomeScreen(onContinue: () -> Unit) {
        Text("Onkod", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("Onkod Keyboard is designed for Somali, English, and French typing on Android.")
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
    }

    @Composable
    private fun EnableScreen(enabled: Boolean, onOpenSettings: () -> Unit, onNext: () -> Unit) {
        Heading("Enable Keyboard")
        Text(if (enabled) "Onkod appears to be enabled." else "Onkod Keyboard is not enabled yet.")
        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) { Text("Open Android keyboard settings") }
        OutlinedButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("Next") }
    }

    @Composable
    private fun SelectKeyboardScreen(onPicker: () -> Unit, onNext: () -> Unit) {
        Heading("Select Keyboard")
        Text("Open the Android input-method picker and select Onkod Keyboard.")
        Button(onClick = onPicker, modifier = Modifier.fillMaxWidth()) { Text("Show keyboard picker") }
        OutlinedButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("Next") }
    }

    @Composable
    private fun LayoutSelectionScreen(onSettings: () -> Unit, onPreview: () -> Unit) {
        Heading("Select Layout Group")
        Text("QWERTY switches only between Somali and English.")
        Text("ASHERTY switches only between Somali and Français.")
        Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Open settings") }
        OutlinedButton(onClick = onPreview, modifier = Modifier.fillMaxWidth()) { Text("Preview keyboards") }
    }

    @Composable
    private fun PreviewScreen(onSettings: () -> Unit, onPrivacy: () -> Unit, onAbout: () -> Unit) {
        Heading("Keyboard Preview")
        Text("Preview only. The actual keyboard is the native Android IME.")
        listOf(
            KeyboardMode.SOMALI_QWERTY,
            KeyboardMode.ENGLISH_QWERTY,
            KeyboardMode.SOMALI_ASHERTY,
            KeyboardMode.FRENCH_AZERTY
        ).forEach { mode ->
            val layout = KeyboardLayouts.forMode(mode)
            Text("${mode.name}: ${layout.spaceLabel}", fontWeight = FontWeight.Bold)
            Text(layout.letterRows.joinToString("\n") { row -> row.joinToString(" ") { it.label } })
            Spacer(Modifier.height(8.dp))
        }
        Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onPrivacy, modifier = Modifier.weight(1f)) { Text("Privacy") }
            OutlinedButton(onClick = onAbout, modifier = Modifier.weight(1f)) { Text("About") }
        }
    }

    @Composable
    private fun PrivacyScreen(onBack: () -> Unit) {
        Heading("Privacy")
        Text("Typed input is delivered to the active Android text field. Settings are stored locally. Onkod V1 has no account, analytics, advertising, cloud typing service, accessibility service, or clipboard history.")
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }

    @Composable
    private fun AboutScreen(onBack: () -> Unit) {
        Heading("About")
        Text("Onkod Keyboard V1 is an Android-only native Kotlin system keyboard for Somali, English, and French.")
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }

    @Composable
    private fun Heading(text: String) {
        Text(text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }

    private fun isKeyboardEnabled(): Boolean {
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return manager.enabledInputMethodList.any { it.packageName == packageName }
    }

    private fun requestImagePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), REQUEST_IMAGE_PERMISSION)
        }
    }

    private enum class Screen { WELCOME, ENABLE, SELECT, LAYOUT, PREVIEW, PRIVACY, ABOUT }

    private companion object {
        const val REQUEST_IMAGE_PERMISSION = 1001
    }
}
