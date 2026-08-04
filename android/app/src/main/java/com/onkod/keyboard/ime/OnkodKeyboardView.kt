package com.onkod.keyboard.ime

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.onkod.keyboard.R

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
        setPadding(8.dp, 8.dp, 8.dp, bottomSafePadding())
    }

    fun render(
        layout: KeyboardLayout,
        settings: KeyboardSettings,
        shiftState: ShiftState,
        symbolsVisible: Boolean,
        symbolsPage: Int,
        emojiVisible: Boolean,
        activeEmojiCategory: EmojiCategory,
        recentEmojis: List<String>
    ) {
        this.settings = settings
        activeLongPressOptions = layout.longPressOptions
        removeAllViews()
        val palette = palette(settings.theme)
        setBackgroundColor(palette.background)

        if (settings.toolbar) addToolbar(layout.toolbar, palette, emojiVisible)
        when {
            emojiVisible -> addEmojiPanel(palette, activeEmojiCategory, recentEmojis)
            symbolsVisible -> addSymbolsPanel(palette, symbolsPage, layout.spaceLabel)
            else -> addLetters(layout, shiftState, palette)
        }
    }

    fun showMessagePanel(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showClipboardPanel(
        currentText: String?,
        recentClips: List<String>,
        pinnedClips: List<String>,
        selectionMode: ClipboardSelectionMode = ClipboardSelectionMode.NONE,
        selectedClips: Set<String> = emptySet()
    ) {
        removeAllViews()
        val palette = palette(settings.theme)
        setBackgroundColor(palette.background)

        addClipboardTopBar(currentText, pinnedClips, palette, selectionMode, selectedClips)
        addClipboardSection("Recent", recentClips, palette, emptyLabel = "Nothing copied yet", selectionMode, selectedClips)
        addClipboardSection("Pinned", pinnedClips, palette, emptyLabel = "No pinned clips", selectionMode, selectedClips)
    }

    private fun clipLabel(value: String): String =
        value.replace(Regex("\\s+"), " ").let { if (it.length > 54) "${it.take(54)}…" else it }

    private fun addClipboardTopBar(
        currentText: String?,
        pinnedClips: List<String>,
        palette: Palette,
        selectionMode: ClipboardSelectionMode,
        selectedClips: Set<String>
    ) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 46.dp).apply {
                bottomMargin = 6.dp
            }
        }
        if (selectionMode != ClipboardSelectionMode.NONE) {
            row.addView(clipboardTopButton(R.drawable.ic_clipboard_keyboard, "Back", KeyAction.ExitClipboardSelection, palette, 0.75f))
            row.addView(TextView(context).apply {
                text = if (selectedClips.isEmpty()) "○  All" else "✓  All"
                setTextColor(palette.text)
                textSize = 20f
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 2.5f)
                setOnClickListener {
                    feedback()
                    listener?.onKey(KeyAction.SelectAllClipboard)
                }
            })
            row.addView(TextView(context).apply {
                text = selectedClips.size.toString()
                setTextColor(palette.text)
                textSize = 20f
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 0.8f)
            })
            row.addView(TextView(context).apply {
                text = "|"
                setTextColor(palette.secondaryText)
                textSize = 22f
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 0.35f)
            })
            row.addView(TextView(context).apply {
                text = if (selectionMode == ClipboardSelectionMode.DELETE) "Delete" else "Done"
                setTextColor(if (selectedClips.isEmpty()) palette.secondaryText else palette.text)
                textSize = 20f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1.4f)
                setOnClickListener {
                    if (selectedClips.isNotEmpty() || selectionMode == ClipboardSelectionMode.PIN) {
                        feedback()
                        listener?.onKey(
                            if (selectionMode == ClipboardSelectionMode.DELETE) {
                                KeyAction.ConfirmClipboardDeleteSelection
                            } else {
                                KeyAction.ConfirmClipboardPinSelection
                            }
                        )
                    }
                }
            })
            addView(row)
            return
        }

        row.addView(clipboardTopButton(R.drawable.ic_clipboard_keyboard, "Keyboard", KeyAction.Abc, palette, 0.8f))
        row.addView(TextView(context).apply {
            text = "Clipboard"
            setTextColor(palette.text)
            textSize = 22f
            gravity = Gravity.CENTER_VERTICAL
            typeface = android.graphics.Typeface.DEFAULT
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 3.4f)
        })
        row.addView(clipboardTopButton(R.drawable.ic_clipboard_pin, "Pin clipboard text", KeyAction.StartClipboardPinSelection, palette, 0.8f))
        row.addView(clipboardTopButton(R.drawable.ic_clipboard_delete, "Delete clipboard text", KeyAction.StartClipboardDeleteSelection, palette, 0.8f))
        addView(row)
    }

    private fun clipboardTopButton(iconRes: Int, description: String, action: KeyAction, palette: Palette, weight: Float): ImageButton =
        ImageButton(context).apply {
            contentDescription = description
            setImageResource(iconRes)
            setColorFilter(palette.text)
            setBackgroundColor(Color.TRANSPARENT)
            scaleType = ImageView.ScaleType.CENTER
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, weight)
            setOnClickListener {
                feedback()
                listener?.onKey(action)
            }
        }

    private fun addClipboardSection(
        title: String,
        clips: List<String>,
        palette: Palette,
        emptyLabel: String,
        selectionMode: ClipboardSelectionMode,
        selectedClips: Set<String>
    ) {
        addView(TextView(context).apply {
            text = title
            setTextColor(palette.secondaryText)
            textSize = 15f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 28.dp)
        })
        val scroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = OVER_SCROLL_NEVER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 80.dp).apply {
                bottomMargin = 10.dp
            }
        }
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
        }
        val visibleClips = clips.take(12)
        if (visibleClips.isEmpty()) {
            row.addView(clipboardCard(emptyLabel, null, palette, muted = true))
        } else {
            visibleClips.forEach { clip ->
                row.addView(
                    clipboardCard(
                        label = clipLabel(clip),
                        action = if (selectionMode == ClipboardSelectionMode.NONE) {
                            KeyAction.PasteText(clip)
                        } else {
                            KeyAction.ToggleClipboardSelection(clip)
                        },
                        palette = palette,
                        muted = false,
                        selected = selectedClips.contains(clip),
                        selectable = selectionMode != ClipboardSelectionMode.NONE
                    )
                )
            }
        }
        scroll.addView(row)
        addView(scroll)
    }

    private fun clipboardCard(
        label: String,
        action: KeyAction?,
        palette: Palette,
        muted: Boolean,
        selected: Boolean = false,
        selectable: Boolean = false
    ): FrameLayout =
        FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(150.dp, 76.dp).apply {
                rightMargin = 10.dp
            }
            if (!muted) {
                background = KeyDrawable(
                    normalColor = if (selected) palette.pressed else palette.key,
                    pressedColor = palette.pressed,
                    radius = 14.dp.toFloat()
                )
            }
            val labelView = TextView(context).apply {
                text = label
                setTextColor(if (muted) palette.secondaryText else palette.text)
                textSize = 18f
                gravity = if (muted) Gravity.CENTER else Gravity.TOP or Gravity.START
                maxLines = 3
                ellipsize = TextUtils.TruncateAt.END
                setPadding(12.dp, 10.dp, 12.dp, 10.dp)
                layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            }
            addView(labelView)
            if (selectable) {
                addView(TextView(context).apply {
                    text = if (selected) "✓" else "○"
                    setTextColor(if (selected) palette.selectedText else palette.text)
                    textSize = 22f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = KeyDrawable(
                        normalColor = if (selected) palette.accent else Color.TRANSPARENT,
                        pressedColor = palette.accent,
                        radius = 17.dp.toFloat()
                    )
                    layoutParams = FrameLayout.LayoutParams(34.dp, 34.dp, Gravity.START or Gravity.TOP).apply {
                        leftMargin = 6.dp
                        topMargin = 6.dp
                    }
                })
            }
            if (action != null) {
                setOnClickListener {
                    feedback()
                    listener?.onKey(action)
                }
            }
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

    private fun addSymbolsPanel(palette: Palette, page: Int, spaceLabel: String) {
        symbolsRows(page).forEach { addKeyRow(it, palette, 46.dp) }
        addKeyRow(
            listOf(
                KeyboardKey("ABC", KeyAction.Abc, 1.2f),
                KeyboardKey("Globe", KeyAction.Globe, 0.9f),
                KeyboardKey(",", KeyAction.Text(","), 1f),
                KeyboardKey(spaceLabel, KeyAction.Space, 4f),
                KeyboardKey(".", KeyAction.Period, 1f),
                KeyboardKey("Enter", KeyAction.Enter, 1.35f)
            ),
            palette,
            50.dp
        )
    }

    private fun symbolsRows(page: Int): List<List<KeyboardKey>> = if (page == 2) {
        listOf(
            symbolRow("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            symbolRow("`", "~", "\\", "|", "{", "}", "€", "£", "¥", "₩"),
            symbolRow("°", "•", "○", "●", "□", "■", "♤", "♡", "◇", "♧"),
            listOf(KeyboardKey("2/2", KeyAction.SymbolsPage(1), 1.4f)) +
                symbolRow("☆", "▪", "¤", "《", "》", "¡", "¿") +
                KeyboardKey("Backspace", KeyAction.Backspace, 1.4f)
        )
    } else {
        listOf(
            symbolRow("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            symbolRow("+", "×", "÷", "=", "/", "_", "<", ">", "[", "]"),
            symbolRow("!", "@", "#", "$", "%", "^", "&", "*", "(", ")"),
            listOf(KeyboardKey("1/2", KeyAction.SymbolsPage(2), 1.4f)) +
                symbolRow("-", "'", "\"", ":", ";", ",", "?") +
                KeyboardKey("Backspace", KeyAction.Backspace, 1.4f)
        )
    }

    private fun symbolRow(vararg labels: String): List<KeyboardKey> =
        labels.map { KeyboardKey(it, KeyAction.Text(it)) }

    private fun addEmojiPanel(palette: Palette, activeCategory: EmojiCategory, recentEmojis: List<String>) {
        addEmojiCategoryRow(palette, activeCategory)
        val scroller = ScrollView(context).apply {
            isFillViewport = false
            overScrollMode = OVER_SCROLL_NEVER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 150.dp).apply {
                bottomMargin = 6.dp
            }
        }
        val grid = LinearLayout(context).apply {
            orientation = VERTICAL
        }
        emojiRows(activeCategory, recentEmojis).forEach { row ->
            grid.addView(keyRow(row.map { emoji -> KeyboardKey(emoji, KeyAction.Text(emoji)) }, palette, 46.dp, flatTextKeys = true))
        }
        scroller.addView(grid)
        addView(scroller)
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

    private fun addEmojiCategoryRow(palette: Palette, activeCategory: EmojiCategory) {
        val row = row(height = 46.dp)
        emojiCategories.forEach { category ->
            val selected = category.type == activeCategory
            row.addView(
                keyView(
                    key = KeyboardKey(category.icon, KeyAction.EmojiCategorySelect(category.type)),
                    palette = if (selected) {
                        palette.copy(functionKey = palette.accent, text = palette.selectedText)
                    } else {
                        palette.copy(functionKey = palette.background, text = palette.secondaryText)
                    },
                    function = true,
                    textSizeSp = 18f
                )
            )
        }
        addView(row)
    }

    private fun emojiRows(category: EmojiCategory, recentEmojis: List<String>): List<List<String>> = when (category) {
        EmojiCategory.HISTORY -> recentEmojis.chunked(8)
        EmojiCategory.FACES -> rowsOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
            "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩",
            "😘", "😗", "☺️", "😚", "😙", "🥲", "😋", "😛",
            "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🫢", "🫣",
            "🤫", "🤔", "🫡", "🤐", "🤨", "😐", "😑", "😶",
            "😏", "😒", "🙄", "😬", "😮‍💨", "🤥", "😌", "😔",
            "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮",
            "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "🥸",
            "😎", "🤓", "🧐", "😕", "🫤", "😟", "🙁", "☹️",
            "😮", "😯", "😲", "😳", "🥺", "🥹", "😦", "😧",
            "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣",
            "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠",
            "🤬", "😈", "👿", "💀", "☠️", "💩", "🤡", "👻",
            "👽", "🤖", "😺", "😸", "😹", "😻", "😼", "😽"
        )
        EmojiCategory.ANIMALS -> rowsOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
            "🐻‍❄️", "🐨", "🐯", "🦁", "🐮", "🐷", "🐽", "🐸",
            "🐵", "🙈", "🙉", "🙊", "🐒", "🐔", "🐧", "🐦",
            "🐤", "🐣", "🐥", "🦆", "🦅", "🦉", "🦇", "🐺",
            "🐗", "🐴", "🦄", "🐝", "🪱", "🐛", "🦋", "🐌",
            "🐞", "🐜", "🪰", "🪲", "🪳", "🦟", "🦗", "🕷️",
            "🐢", "🐍", "🦎", "🦂", "🦀", "🦞", "🦐", "🦑",
            "🐙", "🦪", "🐠", "🐟", "🐡", "🐬", "🦈", "🐳",
            "🐋", "🐊", "🐆", "🐅", "🐃", "🐂", "🐄", "🦬",
            "🐪", "🐫", "🦙", "🦒", "🐘", "🦣", "🦏", "🦛"
        )
        EmojiCategory.FOOD -> rowsOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇",
            "🍓", "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥",
            "🥝", "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌶️",
            "🫑", "🌽", "🥕", "🫒", "🧄", "🧅", "🥔", "🍠",
            "🥐", "🥯", "🍞", "🥖", "🥨", "🧀", "🥚", "🍳",
            "🧈", "🥞", "🧇", "🥓", "🥩", "🍗", "🍖", "🦴",
            "🌭", "🍔", "🍟", "🍕", "🫓", "🥪", "🥙", "🧆",
            "🌮", "🌯", "🫔", "🥗", "🥘", "🫕", "🥫", "🍝",
            "🍜", "🍲", "🍛", "🍣", "🍱", "🥟", "🦪", "🍤",
            "🍙", "🍚", "🍘", "🍥", "🥠", "🥮", "🍢", "🍡",
            "🍧", "🍨", "🍦", "🥧", "🧁", "🍰", "🎂", "🍮"
        )
        EmojiCategory.HOME -> rowsOf(
            "🏠", "🏡", "🏘️", "🏚️", "🏢", "🏬", "🏣", "🏤",
            "🏥", "🏦", "🏨", "🏪", "🏫", "🏩", "💒", "🏛️",
            "⛪", "🕌", "🕋", "🛕", "🕍", "⛩️", "🛤️", "🛣️",
            "🗾", "🎑", "🏞️", "🌅", "🌄", "🌠", "🎇", "🎆",
            "🌇", "🌆", "🏙️", "🌃", "🌌", "🌉", "🌁", "🛏️",
            "🛋️", "🪑", "🚪", "🪟", "🛁", "🚿", "🚽", "🧻",
            "🧼", "🪥", "🧽", "🧹", "🧺", "🧯", "🛒", "🔑",
            "🗝️", "🧳", "🌂", "☂️", "🧵", "🪡", "🪢", "🧶"
        )
        EmojiCategory.SPORTS -> rowsOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉",
            "🥏", "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍",
            "🏏", "🪃", "🥅", "⛳", "🪁", "🏹", "🎣", "🤿",
            "🥊", "🥋", "🎽", "🛹", "🛼", "🛷", "⛸️", "🥌",
            "🎿", "⛷️", "🏂", "🪂", "🏋️", "🤼", "🤸", "⛹️",
            "🤺", "🤾", "🏌️", "🏇", "🧘", "🏄", "🏊", "🤽",
            "🚣", "🧗", "🚵", "🚴", "🏆", "🥇", "🥈", "🥉",
            "🏅", "🎖️", "🏵️", "🎗️", "🎫", "🎟️", "🎪", "🤹"
        )
        EmojiCategory.BOOKS -> rowsOf(
            "📚", "📖", "📕", "📗", "📘", "📙", "📓", "📔",
            "📒", "📃", "📜", "📄", "📰", "🗞️", "📑", "🔖",
            "🏷️", "💰", "🪙", "💴", "💵", "💶", "💷", "💸",
            "💳", "🧾", "💹", "✉️", "📧", "📨", "📩", "📤",
            "📥", "📦", "📫", "📪", "📬", "📭", "📮", "🗳️",
            "✏️", "✒️", "🖋️", "🖊️", "🖌️", "🖍️", "📝", "💼",
            "📁", "📂", "🗂️", "📅", "📆", "🗒️", "🗓️", "📇",
            "📈", "📉", "📊", "📋", "📌", "📍", "📎", "🖇️",
            "📏", "📐", "✂️", "🗃️", "🗄️", "🗑️", "🔒", "🔓"
        )
        EmojiCategory.SYMBOLS -> rowsOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
            "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖",
            "💘", "💝", "💟", "☮️", "✝️", "☪️", "🕉️", "☸️",
            "✡️", "🔯", "🕎", "☯️", "☦️", "🛐", "⛎", "♈",
            "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐",
            "♑", "♒", "♓", "🆔", "⚛️", "🉑", "☢️", "☣️",
            "📴", "📳", "🈶", "🈚", "🈸", "🈺", "🈷️", "✴️",
            "🆚", "💮", "🉐", "㊙️", "㊗️", "🈴", "🈵", "🈹",
            "🈲", "🅰️", "🅱️", "🆎", "🆑", "🅾️", "🆘", "❌",
            "⭕", "🛑", "⛔", "📛", "🚫", "💯", "💢", "♨️",
            "🚷", "🚯", "🚳", "🚱", "🔞", "📵", "🚭", "❗",
            "❕", "❓", "❔", "‼️", "⁉️", "🔅", "🔆", "〽️",
            "⚠️", "🚸", "🔱", "⚜️", "🔰", "♻️", "✅", "🈯"
        )
        EmojiCategory.FLAGS -> rowsOf(*allFlagEmojis.toTypedArray())
    }

    private fun rowsOf(vararg items: String): List<List<String>> = items.toList().chunked(8)

    private fun addKeyRow(keys: List<KeyboardKey>, palette: Palette, height: Int) {
        addView(keyRow(keys, palette, height, flatTextKeys = false))
    }

    private fun keyRow(keys: List<KeyboardKey>, palette: Palette, height: Int, flatTextKeys: Boolean): LinearLayout {
        val row = row(height)
        keys.forEach { key ->
            val function = key.action !is KeyAction.Text
            val size = when {
                flatTextKeys -> 22f
                key.label.length > 1 && key.label != "Somali" -> 12f
                else -> 16f
            }
            row.addView(keyView(key, palette, function, size, flat = flatTextKeys && !function))
        }
        return row
    }

    private fun row(height: Int): LinearLayout = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, height).apply {
            bottomMargin = 6.dp
        }
    }

    private fun keyView(key: KeyboardKey, palette: Palette, function: Boolean, textSizeSp: Float, flat: Boolean = false): TextView =
        TextView(context).apply {
            val isEmojiCategory = key.action is KeyAction.EmojiCategorySelect
            val isSpace = key.action == KeyAction.Space
            val isGlobe = key.action == KeyAction.Globe
            text = iconLabel(key.label)
            contentDescription = key.label
            gravity = Gravity.CENTER
            setTextColor(palette.text)
            textSize = if (isGlobe) 24f else textSizeSp
            typeface = if (flat) android.graphics.Typeface.DEFAULT else android.graphics.Typeface.DEFAULT_BOLD
            includeFontPadding = false
            minHeight = 42.dp
            if (!flat) {
                background = KeyDrawable(
                    normalColor = if (function) palette.functionKey else palette.key,
                    pressedColor = palette.pressed,
                    radius = when {
                        isEmojiCategory -> 22.dp.toFloat()
                        isSpace -> 14.dp.toFloat()
                        else -> 12.dp.toFloat()
                    }
                )
            }
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, key.weight).apply {
                leftMargin = if (isEmojiCategory) 4.dp else 3.dp
                rightMargin = if (isEmojiCategory) 4.dp else 3.dp
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
        "Globe" -> "♁"
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
            Palette(
                background = Color.rgb(26, 27, 31),
                key = Color.rgb(45, 48, 54),
                functionKey = Color.rgb(55, 59, 66),
                text = Color.WHITE,
                secondaryText = Color.rgb(230, 233, 239),
                selectedText = Color.WHITE,
                pressed = Color.rgb(85, 91, 102),
                accent = Color.rgb(37, 99, 235)
            )
        } else {
            Palette(
                background = Color.rgb(232, 236, 242),
                key = Color.WHITE,
                functionKey = Color.rgb(219, 224, 232),
                text = Color.rgb(20, 24, 31),
                secondaryText = Color.rgb(50, 56, 66),
                selectedText = Color.WHITE,
                pressed = Color.rgb(196, 207, 226),
                accent = Color.rgb(37, 99, 235)
            )
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    private fun bottomSafePadding(): Int {
        val navigationInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rootWindowInsets?.getInsets(WindowInsets.Type.navigationBars())?.bottom ?: 0
        } else {
            0
        }
        return maxOf(72.dp, navigationInset + 16.dp)
    }
}

private data class Palette(
    val background: Int,
    val key: Int,
    val functionKey: Int,
    val text: Int,
    val secondaryText: Int,
    val selectedText: Int,
    val pressed: Int,
    val accent: Int
)

private data class EmojiCategoryTab(
    val type: EmojiCategory,
    val icon: String
)

private val emojiCategories = listOf(
    EmojiCategoryTab(EmojiCategory.HISTORY, "◴"),
    EmojiCategoryTab(EmojiCategory.FACES, "☺"),
    EmojiCategoryTab(EmojiCategory.ANIMALS, "♧"),
    EmojiCategoryTab(EmojiCategory.FOOD, "♨"),
    EmojiCategoryTab(EmojiCategory.HOME, "⌂"),
    EmojiCategoryTab(EmojiCategory.SPORTS, "◉"),
    EmojiCategoryTab(EmojiCategory.BOOKS, "▯"),
    EmojiCategoryTab(EmojiCategory.SYMBOLS, "!?"),
    EmojiCategoryTab(EmojiCategory.FLAGS, "⚑")
)

private val allFlagEmojis = listOf(
    "🇸🇴", "🇩🇯", "🇰🇪", "🇪🇹", "🇪🇷", "🇺🇸", "🇬🇧", "🇫🇷",
    "🇦🇩", "🇦🇪", "🇦🇫", "🇦🇬", "🇦🇮", "🇦🇱", "🇦🇲", "🇦🇴",
    "🇦🇶", "🇦🇷", "🇦🇸", "🇦🇹", "🇦🇺", "🇦🇼", "🇦🇽", "🇦🇿",
    "🇧🇦", "🇧🇧", "🇧🇩", "🇧🇪", "🇧🇫", "🇧🇬", "🇧🇭", "🇧🇮",
    "🇧🇯", "🇧🇱", "🇧🇲", "🇧🇳", "🇧🇴", "🇧🇶", "🇧🇷", "🇧🇸",
    "🇧🇹", "🇧🇻", "🇧🇼", "🇧🇾", "🇧🇿", "🇨🇦", "🇨🇨", "🇨🇩",
    "🇨🇫", "🇨🇬", "🇨🇭", "🇨🇮", "🇨🇰", "🇨🇱", "🇨🇲", "🇨🇳",
    "🇨🇴", "🇨🇵", "🇨🇷", "🇨🇺", "🇨🇻", "🇨🇼", "🇨🇽", "🇨🇾",
    "🇨🇿", "🇩🇪", "🇩🇬", "🇩🇰", "🇩🇲", "🇩🇴", "🇩🇿", "🇪🇦",
    "🇪🇨", "🇪🇪", "🇪🇬", "🇪🇭", "🇪🇸", "🇪🇺", "🇫🇮", "🇫🇯",
    "🇫🇰", "🇫🇲", "🇫🇴", "🇬🇦", "🇬🇩", "🇬🇪", "🇬🇫", "🇬🇬",
    "🇬🇭", "🇬🇮", "🇬🇱", "🇬🇲", "🇬🇳", "🇬🇵", "🇬🇶", "🇬🇷",
    "🇬🇸", "🇬🇹", "🇬🇺", "🇬🇼", "🇬🇾", "🇭🇰", "🇭🇲", "🇭🇳",
    "🇭🇷", "🇭🇹", "🇭🇺", "🇮🇨", "🇮🇩", "🇮🇪", "🇮🇱", "🇮🇲",
    "🇮🇳", "🇮🇴", "🇮🇶", "🇮🇷", "🇮🇸", "🇮🇹", "🇯🇪", "🇯🇲",
    "🇯🇴", "🇯🇵", "🇰🇬", "🇰🇭", "🇰🇮", "🇰🇲", "🇰🇳", "🇰🇵",
    "🇰🇷", "🇰🇼", "🇰🇾", "🇰🇿", "🇱🇦", "🇱🇧", "🇱🇨", "🇱🇮",
    "🇱🇰", "🇱🇷", "🇱🇸", "🇱🇹", "🇱🇺", "🇱🇻", "🇱🇾", "🇲🇦",
    "🇲🇨", "🇲🇩", "🇲🇪", "🇲🇫", "🇲🇬", "🇲🇭", "🇲🇰", "🇲🇱",
    "🇲🇲", "🇲🇳", "🇲🇴", "🇲🇵", "🇲🇶", "🇲🇷", "🇲🇸", "🇲🇹",
    "🇲🇺", "🇲🇻", "🇲🇼", "🇲🇽", "🇲🇾", "🇲🇿", "🇳🇦", "🇳🇨",
    "🇳🇪", "🇳🇫", "🇳🇬", "🇳🇮", "🇳🇱", "🇳🇴", "🇳🇵", "🇳🇷",
    "🇳🇺", "🇳🇿", "🇴🇲", "🇵🇦", "🇵🇪", "🇵🇫", "🇵🇬", "🇵🇭",
    "🇵🇰", "🇵🇱", "🇵🇲", "🇵🇳", "🇵🇷", "🇵🇸", "🇵🇹", "🇵🇼",
    "🇵🇾", "🇶🇦", "🇷🇪", "🇷🇴", "🇷🇸", "🇷🇺", "🇷🇼", "🇸🇦",
    "🇸🇧", "🇸🇨", "🇸🇩", "🇸🇪", "🇸🇬", "🇸🇭", "🇸🇮", "🇸🇯",
    "🇸🇰", "🇸🇱", "🇸🇲", "🇸🇳", "🇸🇷", "🇸🇸", "🇸🇹", "🇸🇻",
    "🇸🇽", "🇸🇾", "🇸🇿", "🇹🇦", "🇹🇨", "🇹🇩", "🇹🇫", "🇹🇬",
    "🇹🇭", "🇹🇯", "🇹🇰", "🇹🇱", "🇹🇲", "🇹🇳", "🇹🇴", "🇹🇷",
    "🇹🇹", "🇹🇻", "🇹🇼", "🇹🇿", "🇺🇦", "🇺🇬", "🇺🇲", "🇺🇳",
    "🇺🇾", "🇺🇿", "🇻🇦", "🇻🇨", "🇻🇪", "🇻🇬", "🇻🇮", "🇻🇳",
    "🇻🇺", "🇼🇫", "🇼🇸", "🇽🇰", "🇾🇪", "🇾🇹", "🇿🇦", "🇿🇲",
    "🇿🇼", "🏳️", "🏴", "🏁", "🚩", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️"
)
