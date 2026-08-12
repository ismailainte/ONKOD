package com.somalikeyboard.app.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.somalikeyboard.app.keyboard.KeyboardController
import com.somalikeyboard.app.keyboard.KeyboardState
import com.somalikeyboard.app.keyboard.Page
import com.somalikeyboard.app.keyboard.keyboardColorsFor

/**
 * The complete keyboard surface: toolbar, active page, home indicator, and the transient
 * toast bubble. Used both by [com.somalikeyboard.app.ime.SomaliKeyboardService] (the real
 * IME) and the live preview embedded in [com.somalikeyboard.app.MainActivity].
 */
@Composable
fun KeyboardScreen(
    state: KeyboardState,
    controller: KeyboardController,
    modifier: Modifier = Modifier,
) {
    val colors = keyboardColorsFor(state.resolvedTheme)

    Box(modifier = modifier.fillMaxWidth().background(colors.bg)) {
        Column(Modifier.fillMaxWidth()) {
            Toolbar(colors, controller)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaddingValues(start = 7.dp, end = 7.dp, top = 9.dp, bottom = 11.dp)),
            ) {
                when (state.page) {
                    Page.ABC -> AbcPage(state, colors, controller)
                    Page.SYM1 -> SymbolsPage1(state, colors, controller)
                    Page.SYM2 -> SymbolsPage2(state, colors, controller)
                    Page.EMOJI -> EmojiPage(state, colors, controller)
                    Page.CLIP -> ClipboardPage(state, colors, controller)
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .width(108.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.fg2.copy(alpha = 0.55f)),
                )
            }
        }

        Box(Modifier.align(Alignment.TopCenter).offset(y = (-44).dp)) {
            ToastOverlay(state.toast)
        }
    }
}
