package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somalikeyboard.app.keyboard.EmojiCategory
import com.somalikeyboard.app.keyboard.EmojiData
import com.somalikeyboard.app.keyboard.KeyboardColors
import com.somalikeyboard.app.keyboard.KeyboardController
import com.somalikeyboard.app.keyboard.KeyboardState
import com.somalikeyboard.app.keyboard.Layouts

private fun iconFor(category: EmojiCategory): ImageVector = when (category) {
    EmojiCategory.RECENT -> Icons.Filled.History
    EmojiCategory.SMILEY -> Icons.Filled.EmojiEmotions
    EmojiCategory.ANIMAL -> Icons.Filled.Pets
    EmojiCategory.FOOD -> Icons.Filled.Restaurant
    EmojiCategory.TRAVEL -> Icons.Filled.Flight
    EmojiCategory.SPORT -> Icons.Filled.SportsSoccer
    EmojiCategory.OBJECT -> Icons.Filled.Category
    EmojiCategory.SYMBOL -> Icons.Filled.Tag
    EmojiCategory.FLAG -> Icons.Filled.Flag
}

@Composable
fun EmojiPage(state: KeyboardState, colors: KeyboardColors, controller: KeyboardController) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabChip("search", controller) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search emoji",
                    tint = colors.fg.copy(alpha = 0.55f),
                    modifier = Modifier.size(19.dp),
                )
            }
            Box(Modifier.width(1.dp).height(16.dp).background(colors.line))
            EmojiCategory.entries.forEach { category ->
                val selected = state.category == category
                TabChip("cat:${category.id}", controller) {
                    Icon(
                        iconFor(category),
                        contentDescription = category.id,
                        tint = if (selected) colors.accent else colors.fg.copy(alpha = 0.55f),
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.fillMaxWidth().height(166.dp),
        ) {
            items(EmojiData.forCategory(state.category)) { emoji ->
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .keyPressGesture(emoji, controller),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, fontSize = 23.sp)
                }
            }
        }

        KeyRow(Layouts.emojiBottomRow, state, colors, controller, applyCase = false)
    }
}
