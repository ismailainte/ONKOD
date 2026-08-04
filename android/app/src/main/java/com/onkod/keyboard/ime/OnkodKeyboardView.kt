package com.onkod.keyboard.ime

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.MotionEvent
import android.widget.PopupWindow
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class OnkodKeyboardView(context: Context) : LinearLayout(context) {
    interface Listener {
        fun onKey(action: KeyAction)
        fun onBackspaceHoldStart()
        fun onBackspaceHoldEnd()
        fun onGlobeLongPress()
    }

    var listener: Listener? = null
    private var settings: KeyboardSettings = KeyboardSettings()
    private var activeLongPressOptions: Map<String, List<String>> = emptyMap()
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    init {
        orientation = VERTICAL
        setPadding(6.dp, 6.dp, 6.dp, 6.dp)
    }

    fun render(
        layout: KeyboardLayout,
        settings: KeyboardSettings,
        shiftState: ShiftState,
        symbolsVisible: Boolean,
        emojiVisible: Boolean
    ) {
        this.settings = settings
        activeLongPressOptions = layout.longPressOptions
        removeAllViews()
        val palette = palette(settings.theme)
        setBackgroundColor(palette.background)

        if (settings.toolbar) addToolbar(layout.toolbar, palette, emojiVisible)
        when {
            emojiVisible -> addEmojiPanel(palette)
            symbolsVisible -> addSymbolsPanel(palette)
            else -> addLetters(layout, shiftState, palette)
        }
    }

    fun showMessagePanel(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun addToolbar(keys: List<KeyboardKey>, palette: Palette, emojiVisible: Boolean) {
        val row = row(height = 38.dp)
        keys.forEach { key ->
            val toolbarKey = if (emojiVisible && key.action == KeyAction.EmojiPanel) {
                KeyboardKey("ABC", KeyAction.Abc, key.weight)
            } else {
                key
            }
            row.addView(keyView(toolbarKey, palette, function = true, textSizeSp = 12f))
        }
        addView(row)
    }

    private fun addLetters(layout: KeyboardLayout, shiftState: ShiftState, palette: Palette) {
        if (settings.numberRow) addKeyRow(layout.numberRow, palette, 44.dp)
        layout.letterRows.forEach { keys ->
            val rendered = keys.map { key ->
                if (key.action is KeyAction.Text) key.copy(label = displayLabel(key.label, shiftState)) else key
            }
            addKeyRow(rendered, palette, 48.dp)
        }
        addKeyRow(layout.bottomRow, palette, 50.dp)
    }

    private fun addSymbolsPanel(palette: Palette) {
        KeyboardLayouts.symbolsRows.forEach { addKeyRow(it, palette, 46.dp) }
        addKeyRow(
            listOf(
                KeyboardKey("ABC", KeyAction.Abc, 1.2f),
                KeyboardKey("Somali", KeyAction.Space, 4f),
                KeyboardKey("Enter", KeyAction.Enter, 1.4f),
                KeyboardKey("Backspace", KeyAction.Backspace, 1.4f)
            ),
            palette,
            50.dp
        )
    }

    private fun addEmojiPanel(palette: Palette) {
        val emojis = listOf("😀", "😂", "❤️", "👍", "🙏", "🔥", "🎉", "✅")
            .map { KeyboardKey(it, KeyAction.Text(it)) }
        addKeyRow(emojis, palette, 52.dp)
        addKeyRow(
            listOf(
                KeyboardKey("ABC", KeyAction.Abc, 1.2f),
                KeyboardKey("Somali", KeyAction.Space, 4f),
                KeyboardKey("Backspace", KeyAction.Backspace, 1.4f)
            ),
            palette,
            50.dp
        )
    }

    private fun addKeyRow(keys: List<KeyboardKey>, palette: Palette, height: Int) {
        val row = row(height)
        keys.forEach { key ->
            val function = key.action !is KeyAction.Text
            val size = if (key.label.length > 1 && key.label != "Somali") 12f else 16f
            row.addView(keyView(key, palette, function, size))
        }
        addView(row)
    }

    private fun row(height: Int): LinearLayout = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, height).apply {
            bottomMargin = 6.dp
        }
    }

    private fun keyView(key: KeyboardKey, palette: Palette, function: Boolean, textSizeSp: Float): TextView =
        TextView(context).apply {
            text = iconLabel(key.label)
            contentDescription = key.label
            gravity = Gravity.CENTER
            setTextColor(palette.text)
            textSize = textSizeSp
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            includeFontPadding = false
            minHeight = 42.dp
            background = KeyDrawable(
                normalColor = if (function) palette.functionKey else palette.key,
                pressedColor = palette.pressed,
                radius = 7.dp.toFloat()
            )
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, key.weight).apply {
                leftMargin = 3.dp
                rightMargin = 3.dp
            }
            setOnClickListener {
                feedback()
                listener?.onKey(key.action)
            }
            if (key.action == KeyAction.Backspace) {
                setOnTouchListener { _, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            listener?.onBackspaceHoldStart()
                            false
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            listener?.onBackspaceHoldEnd()
                            false
                        }
                        else -> false
                    }
                }
            }
            if (key.action == KeyAction.Globe) {
                setOnLongClickListener {
                    listener?.onGlobeLongPress()
                    true
                }
            }
            if (key.action is KeyAction.Text) {
                val options = activeLongPressOptions[key.label.uppercase()].orEmpty()
                if (options.isNotEmpty()) {
                    setOnLongClickListener {
                        showAccentPopup(this, options)
                        true
                    }
                }
            }
        }

    private fun showAccentPopup(anchor: TextView, options: List<String>) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(6.dp, 6.dp, 6.dp, 6.dp)
            setBackgroundColor(0xFF111827.toInt())
        }
        val popup = PopupWindow(row, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true)
        options.forEach { option ->
            row.addView(TextView(context).apply {
                text = option
                textSize = 20f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                minWidth = 44.dp
                minHeight = 44.dp
                setOnClickListener {
                    listener?.onKey(KeyAction.Text(option))
                    popup.dismiss()
                }
            })
        }
        popup.isOutsideTouchable = true
        popup.showAsDropDown(anchor, 0, -anchor.height * 2)
    }

    private fun displayLabel(label: String, shiftState: ShiftState): String =
        if (shiftState == ShiftState.LOWERCASE) label.lowercase() else label.uppercase()

    private fun iconLabel(label: String): String = when (label) {
        "Shift" -> "⇧"
        "Backspace" -> "⌫"
        "Globe" -> "◎"
        "Hide" -> "⌄"
        "Enter" -> "↵"
        "Emoji" -> "☺"
        "Settings" -> "⚙"
        "Clipboard" -> "□"
        "More" -> "⋯"
        else -> label
    }

    private fun feedback() {
        if (settings.vibration && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(14, VibrationEffect.DEFAULT_AMPLITUDE))
        } else if (settings.vibration) {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(14)
        }
    }

    private fun palette(theme: ThemeMode): Palette {
        val dark = when (theme) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        return if (dark) {
            Palette(Color.rgb(17, 19, 23), Color.rgb(43, 47, 54), Color.rgb(58, 64, 72), Color.WHITE, Color.rgb(11, 95, 255))
        } else {
            Palette(Color.rgb(215, 220, 227), Color.WHITE, Color.rgb(229, 231, 235), Color.rgb(17, 24, 39), Color.rgb(11, 95, 255))
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}

private data class Palette(
    val background: Int,
    val key: Int,
    val functionKey: Int,
    val text: Int,
    val pressed: Int
)
