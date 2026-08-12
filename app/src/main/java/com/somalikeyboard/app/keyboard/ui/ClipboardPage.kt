package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somalikeyboard.app.keyboard.ClipItem
import com.somalikeyboard.app.keyboard.KeyboardColors
import com.somalikeyboard.app.keyboard.KeyboardController
import com.somalikeyboard.app.keyboard.KeyboardState

@Composable
fun ClipboardPage(state: KeyboardState, colors: KeyboardColors, controller: KeyboardController) {
    val recent = state.clips.filter { !it.pinned }
    val pinned = state.clips.filter { it.pinned }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.size(30.dp).clip(CircleShape).keyPressGesture("abc", controller),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Keyboard, contentDescription = "Back to keyboard", tint = colors.fg, modifier = Modifier.size(20.dp))
            }
            Box(Modifier.width(1.dp).height(18.dp).background(colors.line))
            Text(
                "Clipboard",
                color = colors.fg,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier.size(30.dp).clip(CircleShape).keyPressGesture("clipsave", controller),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Save current text", tint = colors.fg, modifier = Modifier.size(19.dp))
            }
            Box(
                Modifier.size(30.dp).clip(CircleShape).keyPressGesture("clipclear", controller),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear recent clips", tint = colors.fg, modifier = Modifier.size(19.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(212.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Recent", color = colors.fg2, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            ClipGrid(recent, colors, controller, pinnedSection = false)
            Text("Pinned", color = colors.fg2, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
            ClipGrid(pinned, colors, controller, pinnedSection = true)
        }
    }
}

@Composable
private fun ClipGrid(
    clips: List<ClipItem>,
    colors: KeyboardColors,
    controller: KeyboardController,
    pinnedSection: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        clips.chunked(2).forEach { pair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                pair.forEach { clip ->
                    ClipCard(clip, colors, controller, pinnedSection, modifier = Modifier.weight(1f))
                }
                if (pair.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ClipCard(
    clip: ClipItem,
    colors: KeyboardColors,
    controller: KeyboardController,
    pinnedSection: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.app2)
            .keyPressGesture("clip:${clip.id}", controller)
            .padding(9.dp),
    ) {
        Text(
            clip.text,
            color = colors.fg,
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            if (pinnedSection) "unpin" else "pin",
            color = if (pinnedSection) colors.accent else colors.fg2,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.End)
                .keyPressGesture("pin:${clip.id}", controller),
        )
    }
}
