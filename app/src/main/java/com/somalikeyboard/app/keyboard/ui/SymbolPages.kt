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
fun SymbolsPage1(state: KeyboardState, colors: KeyboardColors, controller: KeyboardController) {
    SymbolsPage(state, colors, controller, Layouts.symbolsPage1)
}

@Composable
fun SymbolsPage2(state: KeyboardState, colors: KeyboardColors, controller: KeyboardController) {
    SymbolsPage(state, colors, controller, Layouts.symbolsPage2)
}

@Composable
private fun SymbolsPage(
    state: KeyboardState,
    colors: KeyboardColors,
    controller: KeyboardController,
    rows: List<List<com.somalikeyboard.app.keyboard.RowItem>>,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        KeyRow(Layouts.numberRow, state, colors, controller, applyCase = false)
        rows.forEach { row ->
            KeyRow(row, state, colors, controller, applyCase = false)
        }
        KeyRow(Layouts.symBottomRow, state, colors, controller, applyCase = false)
    }
}
