package com.somalikeyboard.app.preview

import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somalikeyboard.app.keyboard.EnterMode
import com.somalikeyboard.app.keyboard.KeyboardColors
import com.somalikeyboard.app.keyboard.KeyboardController
import com.somalikeyboard.app.keyboard.KeyboardState
import com.somalikeyboard.app.keyboard.LocalTextTarget
import com.somalikeyboard.app.keyboard.ThemeMode
import com.somalikeyboard.app.keyboard.audio.FeedbackPlayer
import com.somalikeyboard.app.keyboard.keyboardColorsFor
import com.somalikeyboard.app.keyboard.ui.KeyboardScreen
import com.somalikeyboard.app.settings.SettingsStore

/**
 * Recreates the phone-frame mockup from the original design 1:1 (status bar, app header,
 * Message/Search tabs, textarea) with the real, interactive [KeyboardScreen] embedded below
 * it — driven by a local text field instead of an [android.view.inputmethod.InputConnection]
 * so the whole thing works as a live, no-setup-required preview of the actual keyboard.
 */
@Composable
fun PreviewScreen() {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }
    val state = remember { KeyboardState() }
    val feedback = remember { FeedbackPlayer(context) }
    val fieldState = remember { mutableStateOf(TextFieldValue("")) }
    val target = remember { LocalTextTarget(fieldState) }
    val scope = rememberCoroutineScope()
    val controller = remember { KeyboardController(state, target, feedback, scope) }

    DisposableEffect(Unit) { onDispose { feedback.release() } }

    val systemDark = isSystemInDarkTheme()
    LaunchedEffect(systemDark) { state.systemDark = systemDark }
    LaunchedEffect(Unit) {
        state.haptics = settings.haptics
        state.sound = settings.sound
    }
    LaunchedEffect(state.haptics) { settings.haptics = state.haptics }
    LaunchedEffect(state.sound) { settings.sound = state.sound }
    LaunchedEffect(fieldState.value) { state.charCount = fieldState.value.text.length }

    val colors = keyboardColorsFor(state.resolvedTheme)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EnableKeyboardBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .width(393.dp)
                .height(852.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(colors.app),
        ) {
            Column(Modifier.fillMaxSize()) {
                FakeStatusBar(colors)
                AppHeader(state, controller, colors)
                ModeTabs(state, controller, colors)
                TextAreaSection(
                    state = state,
                    fieldState = fieldState,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                )
                KeyboardScreen(state = state, controller = controller)
            }
        }
    }
}

@Composable
private fun EnableKeyboardBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1C1E))
            .padding(16.dp),
    ) {
        Text("Enable Somali Keyboard", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Turn it on once in system settings, then switch to it from any text field.",
            color = Color(0xFFAEAEB2),
            fontSize = 12.5.sp,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }) {
                Text("Keyboard Settings")
            }
            Button(onClick = {
                val imm = context.getSystemService(InputMethodManager::class.java)
                imm?.showInputMethodPicker()
            }) {
                Text("Switch Keyboard")
            }
        }
    }
}

@Composable
private fun FakeStatusBar(colors: KeyboardColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("9:41", color = colors.fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SignalCellularAlt, contentDescription = null, tint = colors.fg, modifier = Modifier.size(14.dp))
            Icon(Icons.Filled.Wifi, contentDescription = null, tint = colors.fg, modifier = Modifier.size(14.dp))
            Icon(Icons.Filled.BatteryFull, contentDescription = null, tint = colors.fg, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AppHeader(state: KeyboardState, controller: KeyboardController, colors: KeyboardColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(colors.accent),
            contentAlignment = Alignment.Center,
        ) {
            Text("SO", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text("Kiiboodhka Soomaaliga", color = colors.fg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("Somali keyboard · SH · DH · KH", color = colors.fg2, fontSize = 11.5.sp)
        }
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(colors.app2)
                .clickable(onClick = controller::toggleTheme),
            contentAlignment = Alignment.Center,
        ) {
            val icon = if (state.resolvedTheme == ThemeMode.DARK) Icons.Filled.DarkMode else Icons.Filled.LightMode
            Icon(icon, contentDescription = "Toggle theme", tint = colors.fg, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun ModeTabs(state: KeyboardState, controller: KeyboardController, colors: KeyboardColors) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ModeTab(
            label = "Message",
            selected = state.resolvedEnterMode == EnterMode.ENTER,
            colors = colors,
            modifier = Modifier.weight(1f),
            onClick = { controller.setEnterModeOverride(EnterMode.ENTER) },
        )
        ModeTab(
            label = "Search",
            selected = state.resolvedEnterMode == EnterMode.SEARCH,
            colors = colors,
            modifier = Modifier.weight(1f),
            onClick = { controller.setEnterModeOverride(EnterMode.SEARCH) },
        )
    }
}

@Composable
private fun ModeTab(
    label: String,
    selected: Boolean,
    colors: KeyboardColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(colors.app2)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) colors.accent else colors.fg,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TextAreaSection(
    state: KeyboardState,
    fieldState: androidx.compose.runtime.MutableState<TextFieldValue>,
    colors: KeyboardColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.app2)
                .padding(14.dp),
        ) {
            if (fieldState.value.text.isEmpty()) {
                Text("Ku qor halkan…", color = colors.fg2, fontSize = 16.sp)
            }
            // The on-screen keyboard below drives this field directly; disabling the platform
            // text-input connection here keeps the system IME from popping up and covering it.
            CompositionLocalProvider(LocalTextInputService provides null) {
                BasicTextField(
                    value = fieldState.value,
                    onValueChange = { fieldState.value = it },
                    textStyle = TextStyle(color = colors.fg, fontSize = 16.sp, lineHeight = 23.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 2.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Haptics ${if (state.haptics) "on" else "off"}",
                color = colors.fg2,
                fontSize = 11.sp,
                modifier = Modifier.clickable { state.haptics = !state.haptics },
            )
            Text(
                "Sound ${if (state.sound) "on" else "off"}",
                color = colors.fg2,
                fontSize = 11.sp,
                modifier = Modifier.clickable { state.sound = !state.sound },
            )
            Spacer(Modifier.weight(1f))
            Text("${state.charCount} chars", color = colors.fg2, fontSize = 11.sp)
        }
    }
}
