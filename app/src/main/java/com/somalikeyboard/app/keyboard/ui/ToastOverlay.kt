package com.somalikeyboard.app.keyboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Matches the `toastIn` keyframe fade+slide-up feedback bubble in the source design. */
@Composable
fun ToastOverlay(message: String, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = message.isNotEmpty(),
        modifier = modifier,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
        exit = fadeOut(),
    ) {
        Text(
            text = message,
            color = Color.White,
            fontSize = 12.5.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xF0141416))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
