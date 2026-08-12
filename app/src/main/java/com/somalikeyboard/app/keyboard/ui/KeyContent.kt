package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somalikeyboard.app.keyboard.EnterMode
import com.somalikeyboard.app.keyboard.KeyboardColors
import com.somalikeyboard.app.keyboard.KeySpec
import com.somalikeyboard.app.keyboard.KeyboardState
import com.somalikeyboard.app.keyboard.Lang
import com.somalikeyboard.app.keyboard.ShiftState

/**
 * Resolves what to draw inside a [KeyCell] for a given [KeySpec]: an icon for the action
 * keys (shift/backspace/globe/enter), the live space label, or the (possibly case-transformed,
 * possibly language-swapped) text label for everything else.
 *
 * `applyCase` mirrors the source design's `[data-shift] [data-pg="abc"] [data-k]` CSS rule:
 * only the "abc" page visually lower/upper-cases key labels based on shift state.
 */
@Composable
fun KeyContent(spec: KeySpec, state: KeyboardState, colors: KeyboardColors, applyCase: Boolean) {
    when (spec.id) {
        "shift" -> ShiftIcon(state.shift, colors)
        "bs" -> Icon(
            Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            tint = colors.fg,
            modifier = Modifier.size(24.dp),
        )
        "globe" -> Icon(
            Icons.Filled.Language,
            contentDescription = "Change language",
            tint = colors.fg,
            modifier = Modifier.size(20.dp),
        )
        "enter" -> EnterIcon(state.resolvedEnterMode, colors)
        "space" -> Text(state.spaceLabel, color = colors.fg2, fontSize = 13.5.sp)
        else -> {
            val base = if (state.lang == Lang.EN && spec.enLabel != null) spec.enLabel else spec.label
            val label = if (applyCase) {
                if (state.shift == ShiftState.NONE) base.lowercase() else base.uppercase()
            } else {
                base
            }
            Text(label, color = colors.fg, fontSize = spec.fontSize.sp)
        }
    }
}

@Composable
private fun ShiftIcon(shift: ShiftState, colors: KeyboardColors) {
    val tint = if (shift == ShiftState.NONE) colors.fg else colors.accent
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.ArrowUpward, contentDescription = "Shift", tint = tint, modifier = Modifier.size(22.dp))
        if (shift == ShiftState.CAPS) {
            Box(
                Modifier
                    .padding(top = 2.dp)
                    .width(14.dp)
                    .height(2.dp)
                    .background(tint),
            )
        }
    }
}

@Composable
private fun EnterIcon(mode: EnterMode, colors: KeyboardColors) {
    val tint = if (mode == EnterMode.SEARCH) colors.accent else colors.fg
    val icon = if (mode == EnterMode.SEARCH) Icons.Filled.Search else Icons.AutoMirrored.Filled.KeyboardReturn
    Icon(icon, contentDescription = "Enter", tint = tint, modifier = Modifier.size(22.dp))
}
