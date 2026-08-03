package com.onkod.keyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)
        setContentView(contentView())
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun contentView(): ScrollView {
        statusText = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFF334155.toInt())
            gravity = Gravity.CENTER
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 28.dp, 24.dp, 28.dp)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        layout.addView(TextView(this).apply {
            text = "Onkod Keyboard"
            textSize = 32f
            setTextColor(0xFF111827.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })
        layout.addView(TextView(this).apply {
            text = "Somali system keyboard for Android. Enable it, select it, choose QWERTY or ASHERTY, then start typing."
            textSize = 16f
            setTextColor(0xFF475569.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 12.dp, 0, 18.dp)
        })
        layout.addView(statusText)
        layout.addView(step("1", "Enable Onkod Keyboard", "Open Android keyboard settings and enable Onkod."))
        layout.addView(button("Open keyboard settings") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        layout.addView(step("2", "Select Onkod Keyboard", "Pick Onkod from the Android input-method picker."))
        layout.addView(button("Show keyboard picker") {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showInputMethodPicker()
        })
        layout.addView(step("3", "Choose layout and theme", "Configure QWERTY or ASHERTY, light/dark/system theme, toolbar, vibration, and sound."))
        layout.addView(button("Open Onkod settings") {
            startActivity(Intent(this, SettingsActivity::class.java))
        })
        layout.addView(step("4", "Privacy", "Onkod processes key presses locally. The MVP has no analytics, account, ads, or cloud typing service."))

        return ScrollView(this).apply { addView(layout) }
    }

    private fun updateStatus() {
        statusText.text = if (isKeyboardEnabled()) {
            "Status: Onkod Keyboard is enabled"
        } else {
            "Status: Onkod Keyboard is not enabled yet"
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_INPUT_METHODS).orEmpty()
        return enabled.contains(packageName)
    }

    private fun step(number: String, title: String, body: String): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 18.dp, 0, 8.dp)
            addView(TextView(this@MainActivity).apply {
                text = "$number. $title"
                textSize = 18f
                setTextColor(0xFF111827.toInt())
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            addView(TextView(this@MainActivity).apply {
                text = body
                textSize = 15f
                setTextColor(0xFF475569.toInt())
                setPadding(0, 4.dp, 0, 0)
            })
        }

    private fun button(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            minHeight = 52.dp
            setOnClickListener { action() }
        }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
