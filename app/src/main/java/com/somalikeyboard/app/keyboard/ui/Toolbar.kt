package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.somalikeyboard.app.keyboard.KeyboardColors
import com.somalikeyboard.app.keyboard.KeyboardController

/** The 3-icon row (emoji / tool shortcut / clipboard) shown above the key pages, always. */
@Composable
fun Toolbar(colors: KeyboardColors, controller: KeyboardController, modifier: Modifier = Modifier) {
    val chipBackground = Color(0.5f, 0.5f, 0.5f, 0.18f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            ToolbarIconChip("emoji", controller, chipBackground) {
                Icon(Icons.Filled.Mood, contentDescription = "Emoji", tint = colors.fg, modifier = Modifier.size(18.dp))
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            ToolbarIconChip("tool", controller, chipBackground) {
                Icon(Icons.Filled.Settings, contentDescription = "Toolbar shortcut", tint = colors.fg, modifier = Modifier.size(19.dp))
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            ToolbarIconChip("clipboard", controller, chipBackground) {
                Icon(Icons.Filled.ContentPaste, contentDescription = "Clipboard", tint = colors.fg, modifier = Modifier.size(19.dp))
            }
        }
    }
}
