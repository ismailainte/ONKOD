package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.somalikeyboard.app.keyboard.KeyboardController

/** Unfilled, icon-only pressable used for emoji category tabs (no key background). */
@Composable
fun RowScope.TabChip(
    id: String,
    controller: KeyboardController,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .keyPressGesture(id, controller),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** Circular filled toolbar icon (emoji/tool/clipboard row above the keys). */
@Composable
fun ToolbarIconChip(
    id: String,
    controller: KeyboardController,
    backgroundColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .keyPressGesture(id, controller),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
