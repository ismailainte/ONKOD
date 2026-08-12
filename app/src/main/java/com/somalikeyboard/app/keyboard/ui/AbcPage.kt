package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.somalikeyboard.app.keyboard.KeyboardColors
import com.somalikeyboard.app.keyboard.KeyboardController
import com.somalikeyboard.app.keyboard.KeyboardState
import com.somalikeyboard.app.keyboard.Layouts

@Composable
fun AbcPage(state: KeyboardState, colors: KeyboardColors, controller: KeyboardController) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        KeyRow(Layouts.numberRow, state, colors, controller, applyCase = true)
        Layouts.layoutRows(state.layout).forEach { row ->
            KeyRow(row, state, colors, controller, applyCase = true)
        }
        KeyRow(Layouts.abcBottomRow, state, colors, controller, applyCase = true)
    }
}
