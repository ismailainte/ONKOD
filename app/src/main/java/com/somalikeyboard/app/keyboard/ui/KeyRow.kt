package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.somalikeyboard.app.keyboard.KeyboardColors
import com.somalikeyboard.app.keyboard.KeyboardController
import com.somalikeyboard.app.keyboard.KeyboardState
import com.somalikeyboard.app.keyboard.RowItem

/** Renders one horizontal row of keys/blanks, 6dp gaps, matching the source design's row CSS. */
@Composable
fun KeyRow(
    row: List<RowItem>,
    state: KeyboardState,
    colors: KeyboardColors,
    controller: KeyboardController,
    applyCase: Boolean,
    height: Dp = 38.dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        row.forEach { item ->
            when (item) {
                is RowItem.Blank -> BlankCell(item.weight)
                is RowItem.K -> KeyCell(item.spec, colors, controller) {
                    KeyContent(item.spec, state, colors, applyCase)
                }
            }
        }
    }
}
