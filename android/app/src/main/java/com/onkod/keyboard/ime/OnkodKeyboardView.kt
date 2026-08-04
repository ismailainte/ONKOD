package com.onkod.keyboard.ime

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
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
    private var keyboardHost: LinearLayout? = null
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
        recentEmojis: List<String>,
        oneHandedSide: OneHandedSide = OneHandedSide.NONE,
        clipboardImagePreview: ClipboardImage? = null,
        clipboardSuggestion: ClipboardSuggestion? = null
    ) {
        this.settings = settings
        activeLongPressOptions = layout.longPressOptions
        removeAllViews()
        keyboardHost = null
        orientation = if (oneHandedSide == OneHandedSide.NONE) VERTICAL else HORIZONTAL
        setPadding(
            if (oneHandedSide == OneHandedSide.NONE) 8.dp else 0,
            if (oneHandedSide == OneHandedSide.NONE) 8.dp else 0,
            if (oneHandedSide == OneHandedSide.NONE) 8.dp else 0,
            if (oneHandedSide == OneHandedSide.NONE) bottomSafePadding() else 0
        )
        val palette = palette(settings.theme)
        setBackgroundColor(palette.background)

        if (oneHandedSide != OneHandedSide.NONE) {
            val sidePanel = oneHandedPanel(oneHandedSide, palette)
            val keyboardColumn = LinearLayout(context).apply {
                orientation = VERTICAL
                setPadding(8.dp, 8.dp, 8.dp, bottomSafePadding())
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 3.2f)
            }
            keyboardHost = keyboardColumn
            if (oneHandedSide == OneHandedSide.RIGHT) addView(sidePanel)
            addView(keyboardColumn)
            if (oneHandedSide == OneHandedSide.LEFT) addView(sidePanel)
        }

        clipboardSuggestion?.let { addClipboardSuggestion(it, palette) }
        if (settings.toolbar) addToolbar(layout.toolbar, palette, emojiVisible, clipboardImagePreview)
        when {
            emojiVisible -> addEmojiPanel(palette, activeEmojiCategory, recentEmojis)
            symbolsVisible -> addSymbolsPanel(palette, symbolsPage, layout.spaceLabel)
            else -> addLetters(layout, shiftState, palette)
        }
        keyboardHost = null
    }

    fun showMessagePanel(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun addKeyboardView(view: android.view.View) {
        (keyboardHost ?: this).addView(view)
    }

    fun showClipboardPanel(
        currentText: String?,
        recentClips: List<ClipboardClip>,
        pinnedClips: List<ClipboardClip>,
        selectionMode: ClipboardSelectionMode = ClipboardSelectionMode.NONE,
        selectedClips: Set<String> = emptySet()
    ) {
        removeAllViews()
        keyboardHost = null
        orientation = VERTICAL
        setPadding(8.dp, 8.dp, 8.dp, bottomSafePadding())
        val palette = palette(settings.theme)
        setBackgroundColor(palette.background)

        addClipboardTopBar(currentText, pinnedClips, palette, selectionMode, selectedClips)
        addClipboardSection("Recent", recentClips, palette, emptyLabel = "Nothing copied yet", selectionMode, selectedClips)
        addClipboardSection("Pinned", pinnedClips, palette, emptyLabel = "No pinned clips", selectionMode, selectedClips)
    }

    fun showMorePanel(toolbarKeys: List<KeyboardKey>) {
        removeAllViews()
        keyboardHost = null
        orientation = VERTICAL
        setPadding(8.dp, 8.dp, 8.dp, bottomSafePadding())
        val palette = palette(settings.theme)
        setBackgroundColor(palette.background)
        addToolbar(toolbarKeys, palette, emojiVisible = false, clipboardImagePreview = null)

        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.TOP or Gravity.START
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, normalKeyboardBodyHeight())
            setPadding(10.dp, 12.dp, 10.dp, 0)
        }
        row.addView(moreOptionTile(R.drawable.ic_more_one_handed, "One-handed\nkeyboard", KeyAction.OneHandedKeyboard, palette))
        row.addView(moreOptionTile(R.drawable.ic_more_mode, "Mode", KeyAction.Settings, palette))
        addKeyboardView(row)
    }

    private fun clipLabel(value: String): String =
        value.replace(Regex("\\s+"), " ").let { if (it.length > 54) "${it.take(54)}…" else it }

    private fun addClipboardSuggestion(suggestion: ClipboardSuggestion, palette: Palette) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = KeyDrawable(
                normalColor = toolbarSurfaceColor(palette),
                pressedColor = toolbarSurfaceColor(palette),
                radius = 16.dp.toFloat()
            )
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 42.dp).apply {
                bottomMargin = 6.dp
            }
            elevation = 0f
            translationZ = 0f
            stateListAnimator = null
        }

        val pasteAction = when (suggestion) {
            is ClipboardSuggestion.Text -> KeyAction.PasteText(suggestion.value)
            is ClipboardSuggestion.Image -> KeyAction.InsertClipboardImage(suggestion.image)
        }

        val content = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            background = KeyDrawable(
                normalColor = Color.TRANSPARENT,
                pressedColor = palette.pressed,
                radius = 14.dp.toFloat()
            )
            setPadding(12.dp, 0, 12.dp, 0)
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
                rightMargin = 6.dp
            }
            setOnClickListener {
                feedback()
                listener?.onKey(pasteAction)
            }
        }

        when (suggestion) {
            is ClipboardSuggestion.Text -> {
                content.addView(TextView(context).apply {
                    text = if (suggestion.sensitive) "Sensitive content" else clipLabel(suggestion.value)
                    setTextColor(palette.text)
                    textSize = 15f
                    gravity = Gravity.CENTER
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    includeFontPadding = false
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                })
            }
            is ClipboardSuggestion.Image -> {
                content.addView(ImageView(context).apply {
                    contentDescription = "Clipboard image"
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageURI(Uri.parse(suggestion.image.uri))
                    background = KeyDrawable(
                        normalColor = palette.key,
                        pressedColor = palette.key,
                        radius = 10.dp.toFloat()
                    )
                    clipToOutline = true
                    layoutParams = LayoutParams(42.dp, 34.dp)
                })
            }
        }

        row.addView(content)
        row.addView(TextView(context).apply {
            text = "×"
            setTextColor(palette.text)
            textSize = 22f
            gravity = Gravity.CENTER
            includeFontPadding = false
            background = KeyDrawable(
                normalColor = Color.TRANSPARENT,
                pressedColor = toolbarPressedColor(palette),
                radius = 12.dp.toFloat()
            )
            layoutParams = LayoutParams(42.dp, LayoutParams.MATCH_PARENT)
            setOnClickListener {
                feedback()
                listener?.onKey(KeyAction.DismissClipboardSuggestion)
            }
        })
        addKeyboardView(row)
    }

    private fun addClipboardTopBar(
        currentText: String?,
        pinnedClips: List<ClipboardClip>,
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
        addKeyboardView(row)
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

    private fun moreOptionTile(iconRes: Int, label: String, action: KeyAction, palette: Palette): LinearLayout =
        LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            layoutParams = LayoutParams(120.dp, LayoutParams.MATCH_PARENT).apply {
                rightMargin = 16.dp
            }
            addView(ImageButton(context).apply {
                contentDescription = label.replace("\n", " ")
                setImageResource(iconRes)
                setColorFilter(palette.text)
                setBackgroundColor(Color.TRANSPARENT)
                background = KeyDrawable(
                    normalColor = palette.functionKey,
                    pressedColor = palette.pressed,
                    radius = 34.dp.toFloat()
                )
                scaleType = ImageView.ScaleType.CENTER
                layoutParams = LayoutParams(68.dp, 68.dp)
                setOnClickListener {
                    feedback()
                    listener?.onKey(action)
                }
            })
            addView(TextView(context).apply {
                text = label
                setTextColor(palette.text)
                textSize = 16f
                gravity = Gravity.CENTER
                includeFontPadding = false
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    topMargin = 8.dp
                }
                setOnClickListener {
                    feedback()
                    listener?.onKey(action)
                }
            })
        }

    private fun oneHandedPanel(side: OneHandedSide, palette: Palette): LinearLayout =
        LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(58, 58, 60))
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            addView(oneHandedControl("↙↗", "Normal keyboard", KeyAction.ExitOneHandedKeyboard, palette))
            addView(oneHandedControl("‹", "Move keyboard left", KeyAction.SetOneHandedSide(OneHandedSide.LEFT), palette, selected = side == OneHandedSide.LEFT))
            addView(oneHandedControl("›", "Move keyboard right", KeyAction.SetOneHandedSide(OneHandedSide.RIGHT), palette, selected = side == OneHandedSide.RIGHT))
        }

    private fun oneHandedControl(
        label: String,
        description: String,
        action: KeyAction,
        palette: Palette,
        selected: Boolean = false
    ): TextView =
        TextView(context).apply {
            text = label
            contentDescription = description
            setTextColor(if (selected) palette.accent else palette.text)
            textSize = 34f
            gravity = Gravity.CENTER
            includeFontPadding = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 74.dp).apply {
                topMargin = 12.dp
                bottomMargin = 12.dp
            }
            setOnClickListener {
                feedback()
                listener?.onKey(action)
            }
        }

    private fun addClipboardSection(
        title: String,
        clips: List<ClipboardClip>,
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
            minimumWidth = resources.displayMetrics.widthPixels - paddingLeft - paddingRight
        }
        val visibleClips = clips.take(12)
        if (visibleClips.isEmpty()) {
            row.gravity = Gravity.CENTER
            row.addView(clipboardCard(emptyLabel, null, palette, muted = true, fillWidth = true))
        } else {
            visibleClips.forEach { clip ->
                val action = if (selectionMode == ClipboardSelectionMode.NONE) {
                    when (clip) {
                        is ClipboardClip.Text -> KeyAction.PasteText(clip.value)
                        is ClipboardClip.Image -> KeyAction.InsertClipboardImage(clip.image)
                    }
                } else {
                    KeyAction.ToggleClipboardSelection(clip.id)
                }
                val selected = selectedClips.contains(clip.id)
                row.addView(
                    when (clip) {
                        is ClipboardClip.Text -> clipboardCard(
                            label = clipLabel(clip.value),
                            action = action,
                            palette = palette,
                            muted = false,
                            selected = selected,
                            selectable = selectionMode != ClipboardSelectionMode.NONE
                        )
                        is ClipboardClip.Image -> clipboardImageCard(
                            image = clip.image,
                            action = action,
                            palette = palette,
                            selected = selected,
                            selectable = selectionMode != ClipboardSelectionMode.NONE
                        )
                    }
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
        selectable: Boolean = false,
        fillWidth: Boolean = false
    ): FrameLayout =
        FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                if (fillWidth) LinearLayout.LayoutParams.MATCH_PARENT else 150.dp,
                76.dp
            ).apply {
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

    private fun clipboardImageCard(
        image: ClipboardImage,
        action: KeyAction,
        palette: Palette,
        selected: Boolean = false,
        selectable: Boolean = false
    ): FrameLayout =
        FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(150.dp, 76.dp).apply {
                rightMargin = 10.dp
            }
            background = KeyDrawable(
                normalColor = if (selected) palette.pressed else palette.key,
                pressedColor = palette.pressed,
                radius = 14.dp.toFloat()
            )
            addView(ImageView(context).apply {
                contentDescription = "Clipboard image"
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(Uri.parse(image.uri))
                clipToOutline = true
                layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            })
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
            setOnClickListener {
                feedback()
                listener?.onKey(action)
            }
        }

    private fun addToolbar(
        keys: List<KeyboardKey>,
        palette: Palette,
        emojiVisible: Boolean,
        clipboardImagePreview: ClipboardImage?
    ) {
        val row = row(height = 38.dp)
        row.background = KeyDrawable(
            normalColor = toolbarSurfaceColor(palette),
            pressedColor = toolbarSurfaceColor(palette),
            radius = 14.dp.toFloat()
        )
        keys.forEach { key ->
            val toolbarKey = if (emojiVisible && key.action == KeyAction.EmojiPanel) {
                KeyboardKey("ABC", KeyAction.Abc, key.weight)
            } else {
                key
            }
            row.addView(keyView(toolbarKey, palette, function = true, textSizeSp = 14f, toolbarIcon = true))
        }
        clipboardImagePreview?.let { image ->
            row.addView(toolbarImagePreview(image, palette))
        }
        addView(row)
    }

    private fun toolbarImagePreview(image: ClipboardImage, palette: Palette): ImageView =
        ImageView(context).apply {
            contentDescription = "Clipboard image"
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(Uri.parse(image.uri))
            background = KeyDrawable(
                normalColor = toolbarPressedColor(palette),
                pressedColor = palette.pressed,
                radius = 10.dp.toFloat()
            )
            clipToOutline = true
            layoutParams = LayoutParams(44.dp, LayoutParams.MATCH_PARENT).apply {
                leftMargin = 3.dp
                rightMargin = 3.dp
            }
            setOnClickListener {
                feedback()
                listener?.onKey(KeyAction.InsertClipboardImage(image))
            }
        }

    private fun addClipboardImageSection(images: List<ClipboardImage>, palette: Palette) {
        if (images.isEmpty()) return
        addView(TextView(context).apply {
            text = "Images"
            setTextColor(palette.secondaryText)
            textSize = 15f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 28.dp)
        })
        val scroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = OVER_SCROLL_NEVER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 86.dp).apply {
                bottomMargin = 10.dp
            }
        }
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
        }
        images.take(12).forEach { image ->
            row.addView(ImageView(context).apply {
                contentDescription = "Clipboard image"
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(Uri.parse(image.uri))
                background = KeyDrawable(
                    normalColor = palette.key,
                    pressedColor = palette.pressed,
                    radius = 14.dp.toFloat()
                )
                layoutParams = LinearLayout.LayoutParams(86.dp, 80.dp).apply {
                    rightMargin = 10.dp
                }
                setOnClickListener {
                    feedback()
                    listener?.onKey(KeyAction.InsertClipboardImage(image))
                }
            })
        }
        scroll.addView(row)
        addKeyboardView(scroll)
    }

    private fun addLetters(layout: KeyboardLayout, shiftState: ShiftState, palette: Palette) {
        if (settings.numberRow) addKeyRow(layout.numberRow, palette, 46.dp)
        layout.letterRows.forEach { keys ->
            val rendered = keys.map { key ->
                if (key.action is KeyAction.Text) key.copy(label = displayLabel(key.label, shiftState)) else key
            }
            addKeyRow(rendered, palette, 50.dp)
        }
        addKeyRow(layout.bottomRow, palette, 52.dp)
    }

    private fun normalKeyboardBodyHeight(): Int =
        (if (settings.numberRow) 46.dp + 6.dp else 0) +
            (3 * (50.dp + 6.dp)) +
            52.dp + 6.dp

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
        val emojiFrame = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 206.dp).apply {
                bottomMargin = 6.dp
            }
        }
        val scroller = ScrollView(context).apply {
            isFillViewport = false
            overScrollMode = OVER_SCROLL_NEVER
            layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        val grid = LinearLayout(context).apply {
            orientation = VERTICAL
            setPadding(0, 0, 0, 46.dp)
        }
        emojiRows(activeCategory, recentEmojis).forEach { row ->
            grid.addView(keyRow(row.map { emoji -> KeyboardKey(emoji, KeyAction.Text(emoji)) }, palette, 46.dp, flatTextKeys = true))
        }
        scroller.addView(grid)
        emojiFrame.addView(scroller)
        emojiFrame.addView(keyView(KeyboardKey("Backspace", KeyAction.Backspace), palette, function = true, textSizeSp = 16f, flatControl = true).apply {
            layoutParams = FrameLayout.LayoutParams(64.dp, 42.dp, Gravity.BOTTOM or Gravity.END).apply {
                rightMargin = 3.dp
                bottomMargin = 2.dp
            }
        })
        addKeyboardView(emojiFrame)
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
        addKeyboardView(row)
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
        addKeyboardView(keyRow(keys, palette, height, flatTextKeys = false))
    }

    private fun keyRow(keys: List<KeyboardKey>, palette: Palette, height: Int, flatTextKeys: Boolean): LinearLayout {
        val row = row(height)
        keys.forEach { key ->
            val function = key.action !is KeyAction.Text
            val size = when {
                flatTextKeys -> 22f
                key.action == KeyAction.Space -> 20f
                key.action is KeyAction.Text && key.label.length > 1 -> 20f
                key.action is KeyAction.Text -> 24f
                key.action == KeyAction.Symbols -> 18f
                else -> 18f
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

    private fun keyView(
        key: KeyboardKey,
        palette: Palette,
        function: Boolean,
        textSizeSp: Float,
        flat: Boolean = false,
        flatControl: Boolean = false,
        toolbarIcon: Boolean = false
    ): TextView =
        TextView(context).apply {
            val isEmojiCategory = key.action is KeyAction.EmojiCategorySelect
            val isSpace = key.action == KeyAction.Space
            val iconRes = keyIconRes(key.action)
            text = if (iconRes == null) iconLabel(key.label) else ""
            contentDescription = key.label
            gravity = Gravity.CENTER
            setTextColor(palette.text)
            textSize = textSizeSp
            typeface = if (flat) android.graphics.Typeface.DEFAULT else android.graphics.Typeface.DEFAULT_BOLD
            includeFontPadding = false
            minHeight = 42.dp
            elevation = 0f
            translationZ = 0f
            stateListAnimator = null
            if (toolbarIcon) {
                background = KeyDrawable(
                    normalColor = Color.TRANSPARENT,
                    pressedColor = toolbarPressedColor(palette),
                    radius = 14.dp.toFloat()
                )
            } else if (!flat) {
                background = KeyDrawable(
                    normalColor = if (flatControl) flatControlColor(palette) else if (function) palette.functionKey else palette.key,
                    pressedColor = if (flatControl) flatControlPressedColor(palette) else palette.pressed,
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
            if (iconRes != null) {
                context.getDrawable(iconRes)?.mutate()?.let { icon ->
                    icon.setTint(palette.text)
                    foreground = icon
                    foregroundGravity = Gravity.CENTER
                }
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

    private fun keyIconRes(action: KeyAction): Int? = when (action) {
        KeyAction.EmojiPanel -> R.drawable.ic_toolbar_emoji
        KeyAction.Settings -> R.drawable.ic_toolbar_settings
        KeyAction.Clipboard -> R.drawable.ic_toolbar_clipboard
        KeyAction.More -> R.drawable.ic_toolbar_more
        KeyAction.Shift -> R.drawable.ic_key_shift
        KeyAction.Backspace -> R.drawable.ic_key_backspace
        KeyAction.Globe -> R.drawable.ic_key_globe
        KeyAction.Enter -> R.drawable.ic_key_enter
        else -> null
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

    private fun flatControlColor(palette: Palette): Int =
        if (palette.text == Color.WHITE) Color.rgb(32, 34, 38) else Color.rgb(224, 229, 236)

    private fun flatControlPressedColor(palette: Palette): Int =
        if (palette.text == Color.WHITE) Color.rgb(43, 46, 52) else Color.rgb(210, 217, 228)

    private fun toolbarSurfaceColor(palette: Palette): Int =
        if (palette.text == Color.WHITE) Color.rgb(26, 27, 31) else Color.rgb(232, 236, 242)

    private fun toolbarPressedColor(palette: Palette): Int =
        if (palette.text == Color.WHITE) Color.rgb(43, 46, 52) else Color.rgb(210, 217, 228)

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    private fun bottomSafePadding(): Int {
        val navigationInset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rootWindowInsets?.getInsets(WindowInsets.Type.navigationBars())?.bottom ?: 0
        } else {
            0
        }
        return if (navigationInset > 0) 12.dp else 8.dp
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
