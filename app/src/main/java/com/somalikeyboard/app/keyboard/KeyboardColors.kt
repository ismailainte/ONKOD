package com.somalikeyboard.app.keyboard

import androidx.compose.ui.graphics.Color

/**
 * Direct port of the CSS custom properties in `[data-kbroot]` / `[data-kbroot="light"]`
 * from the original design.
 */
data class KeyboardColors(
    val isDark: Boolean,
    val bg: Color,
    val topBar: Color,
    val key: Color,
    val keySpecial: Color,
    val fg: Color,
    val fg2: Color,
    val accent: Color,
    val app: Color,
    val app2: Color,
    val line: Color,
)

val DarkKeyboardColors = KeyboardColors(
    isDark = true,
    bg = Color(0xFF0B0B0B),
    topBar = Color(0xFF141414),
    key = Color(0xFF3A3A3C),
    keySpecial = Color(0xFF1B1B1D),
    fg = Color(0xFFFFFFFF),
    fg2 = Color(0xFF8E8E93),
    accent = Color(0xFF4B8CF7),
    app = Color(0xFF000000),
    app2 = Color(0xFF161618),
    line = Color(0x14FFFFFF),
)

val LightKeyboardColors = KeyboardColors(
    isDark = false,
    bg = Color(0xFFE6E8EC),
    topBar = Color(0xFFE6E8EC),
    key = Color(0xFFFDFDFE),
    keySpecial = Color(0xFFCCD0D8),
    fg = Color(0xFF121316),
    fg2 = Color(0xFF6B7280),
    accent = Color(0xFF1A73E8),
    app = Color(0xFFFFFFFF),
    app2 = Color(0xFFF3F4F6),
    line = Color(0x17000000),
)

fun keyboardColorsFor(mode: ThemeMode): KeyboardColors =
    if (mode == ThemeMode.DARK) DarkKeyboardColors else LightKeyboardColors

/** Matches `[data-k]:active { filter: brightness(x) }` press feedback. */
fun KeyboardColors.pressedVariant(base: Color): Color =
    if (isDark) base.brighten(0.45f) else base.darken(0.10f)

private fun Color.brighten(amount: Float): Color = Color(
    red = (red + (1f - red) * amount).coerceIn(0f, 1f),
    green = (green + (1f - green) * amount).coerceIn(0f, 1f),
    blue = (blue + (1f - blue) * amount).coerceIn(0f, 1f),
    alpha = alpha,
)

private fun Color.darken(amount: Float): Color = Color(
    red = (red * (1f - amount)).coerceIn(0f, 1f),
    green = (green * (1f - amount)).coerceIn(0f, 1f),
    blue = (blue * (1f - amount)).coerceIn(0f, 1f),
    alpha = alpha,
)
