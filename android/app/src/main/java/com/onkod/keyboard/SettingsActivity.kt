package com.onkod.keyboard

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import com.onkod.keyboard.ime.KeyboardSettings
import com.onkod.keyboard.ime.LayoutType
import com.onkod.keyboard.ime.LongPressDelay
import com.onkod.keyboard.ime.SettingsStore
import com.onkod.keyboard.ime.ThemeMode

class SettingsActivity : Activity() {
    private lateinit var store: SettingsStore
    private var settings = KeyboardSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Onkod Settings"
        store = SettingsStore(this)
        settings = store.read()
        setContentView(contentView())
    }

    private fun contentView(): ScrollView {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 24.dp, 24.dp, 24.dp)
        }
        layout.addView(TextView(this).apply {
            text = "Settings"
            textSize = 28f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(0xFF111827.toInt())
        })
        layout.addView(section("Layout"))
        layout.addView(radioGroup(
            values = listOf("QWERTY" to LayoutType.QWERTY, "ASHERTY" to LayoutType.ASHERTY),
            selected = settings.layout,
            onSelected = { save(settings.copy(layout = it)) }
        ))
        layout.addView(section("Theme"))
        layout.addView(radioGroup(
            values = listOf("System" to ThemeMode.SYSTEM, "Light" to ThemeMode.LIGHT, "Dark" to ThemeMode.DARK),
            selected = settings.theme,
            onSelected = { save(settings.copy(theme = it)) }
        ))
        layout.addView(section("Long-press delay"))
        layout.addView(radioGroup(
            values = listOf("Normal" to LongPressDelay.NORMAL, "Short" to LongPressDelay.SHORT, "Long" to LongPressDelay.LONG),
            selected = settings.longPressDelay,
            onSelected = { save(settings.copy(longPressDelay = it)) }
        ))
        layout.addView(toggle("Number row", settings.numberRow) { save(settings.copy(numberRow = it)) })
        layout.addView(toggle("Toolbar", settings.toolbar) { save(settings.copy(toolbar = it)) })
        layout.addView(toggle("Key vibration", settings.vibration) { save(settings.copy(vibration = it)) })
        layout.addView(toggle("Key sound", settings.sound) { save(settings.copy(sound = it)) })
        layout.addView(toggle("Show key preview", settings.keyPreview) { save(settings.copy(keyPreview = it)) })
        layout.addView(Button(this).apply {
            text = "Done"
            setOnClickListener { finish() }
        })
        return ScrollView(this).apply { addView(layout) }
    }

    private fun save(next: KeyboardSettings) {
        settings = store.write(next)
    }

    private fun section(title: String): TextView =
        TextView(this).apply {
            text = title
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(0xFF111827.toInt())
            setPadding(0, 20.dp, 0, 8.dp)
        }

    private fun <T> radioGroup(values: List<Pair<String, T>>, selected: T, onSelected: (T) -> Unit): RadioGroup =
        RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            values.forEachIndexed { index, pair ->
                addView(RadioButton(this@SettingsActivity).apply {
                    id = index + 1
                    text = pair.first
                    isChecked = pair.second == selected
                    setOnClickListener { onSelected(pair.second) }
                })
            }
        }

    private fun toggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12.dp, 0, 12.dp)
            addView(TextView(this@SettingsActivity).apply {
                text = label
                textSize = 16f
                setTextColor(0xFF111827.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(Switch(this@SettingsActivity).apply {
                isChecked = checked
                setOnCheckedChangeListener { _, value -> onChanged(value) }
            })
        }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
