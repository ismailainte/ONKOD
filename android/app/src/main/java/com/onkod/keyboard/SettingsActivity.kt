package com.onkod.keyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onkod.keyboard.ime.KeyboardSettings
import com.onkod.keyboard.ime.LanguageMode
import com.onkod.keyboard.ime.LayoutGroup
import com.onkod.keyboard.ime.LongPressDelay
import com.onkod.keyboard.ime.SettingsStore
import com.onkod.keyboard.ime.ThemeMode

class SettingsActivity : ComponentActivity() {
    private lateinit var store: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = SettingsStore(this)
        setContent { SettingsApp() }
    }

    @Composable
    private fun SettingsApp() {
        var settings by remember { mutableStateOf(store.read()) }
        fun save(next: KeyboardSettings) {
            settings = store.write(next)
        }
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Choice("Layout group", settings.layoutGroup, listOf(
                        "QWERTY: Somali + English" to LayoutGroup.QWERTY,
                        "ASHERTY: Somali + Français" to LayoutGroup.ASHERTY
                    )) { save(settings.copy(layoutGroup = it)) }
                    Choice("Default QWERTY language", settings.qwertyLanguage, listOf(
                        "Somali" to LanguageMode.SOMALI,
                        "English" to LanguageMode.ENGLISH
                    )) { save(settings.copy(qwertyLanguage = it)) }
                    Choice("Default ASHERTY language", settings.ashertyLanguage, listOf(
                        "Somali" to LanguageMode.SOMALI,
                        "Français" to LanguageMode.FRENCH
                    )) { save(settings.copy(ashertyLanguage = it)) }
                    Choice("Theme", settings.theme, listOf(
                        "System" to ThemeMode.SYSTEM,
                        "Light" to ThemeMode.LIGHT,
                        "Dark" to ThemeMode.DARK
                    )) { save(settings.copy(theme = it)) }
                    Choice("Long-press delay", settings.longPressDelay, listOf(
                        "Short" to LongPressDelay.SHORT,
                        "Normal" to LongPressDelay.NORMAL,
                        "Long" to LongPressDelay.LONG
                    )) { save(settings.copy(longPressDelay = it)) }
                    Toggle("Number row", settings.numberRow) { save(settings.copy(numberRow = it)) }
                    Toggle("Toolbar", settings.toolbar) { save(settings.copy(toolbar = it)) }
                    Toggle("Key vibration", settings.vibration) { save(settings.copy(vibration = it)) }
                    Toggle("Key sound", settings.sound) { save(settings.copy(sound = it)) }
                    Toggle("Show key preview", settings.keyPreview) { save(settings.copy(keyPreview = it)) }
                    Button(onClick = { finish() }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                }
            }
        }
    }

    @Composable
    private fun <T> Choice(title: String, selected: T, values: List<Pair<String, T>>, onSelected: (T) -> Unit) {
        Text(title, fontWeight = FontWeight.Bold)
        values.forEach { pair ->
            androidx.compose.foundation.layout.Row {
                RadioButton(selected = pair.second == selected, onClick = { onSelected(pair.second) })
                Text(pair.first, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }

    @Composable
    private fun Toggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
        androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Switch(checked = checked, onCheckedChange = onChanged)
        }
    }
}
