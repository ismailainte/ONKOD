package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.somalikeyboard.app.keyboard.KeyboardColors
import com.somalikeyboard.app.keyboard.KeyboardController
import com.somalikeyboard.app.keyboard.KeySpec
import com.somalikeyboard.app.keyboard.pressedVariant

/**
 * One interactive key cell: a filled, rounded rect that darkens/brightens on press
 * (`[data-k]:active { filter: brightness(...) }` in the source design).
 */
@Composable
fun RowScope.KeyCell(
    spec: KeySpec,
    colors: KeyboardColors,
    controller: KeyboardController,
    content: @Composable BoxScope.() -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val baseColor = if (spec.special) colors.keySpecial else colors.key
    val displayColor = if (pressed) colors.pressedVariant(baseColor) else baseColor

    Box(
        modifier = Modifier
            .weight(spec.weight)
            .fillMaxHeight()
            .clip(RoundedCornerShape(9.dp))
            .background(displayColor)
            .then(if (spec.bottomAlign) Modifier.padding(bottom = 6.dp) else Modifier)
            .keyPressGesture(spec.id, controller) { pressed = it },
        contentAlignment = if (spec.bottomAlign) Alignment.BottomCenter else Alignment.Center,
        content = content,
    )
}

@Composable
fun RowScope.BlankCell(weight: Float) {
    Box(Modifier.weight(weight))
}
