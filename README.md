# Onkod Keyboard

Onkod Keyboard is a native Android Kotlin system keyboard for Somali typing. It is implemented as a real Android Input Method Editor (IME), so users can enable it in Android keyboard settings and type inside other apps such as Messages, WhatsApp, Chrome, Notes, and normal text fields.

The project is now Kotlin-first. React Native and Expo were removed because the actual keyboard must be native Android, and keeping the app shell native makes the build simpler and less fragile.

## Architecture

- `MainActivity`: native Kotlin onboarding and setup screen.
- `SettingsActivity`: native Kotlin settings screen.
- `OnkodInputMethodService`: Android IME service.
- `OnkodKeyboardView`: native keyboard UI built with Android views.
- `KeyboardLayouts`: Somali QWERTY, ASHERTY, and symbols data.
- `SettingsStore`: local SharedPreferences settings shared by app screens and IME.

## Requirements

- Android Studio.
- JDK 17.
- Android SDK with API 35 installed.
- Android device or emulator.

## Build

```powershell
cd android
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

On macOS/Linux:

```bash
cd android
./gradlew test
./gradlew assembleDebug
```

## Install And Enable

1. Build or install the debug APK.
2. Open Onkod Keyboard.
3. Tap **Open keyboard settings** and enable Onkod Keyboard.
4. Tap **Show keyboard picker** and select Onkod Keyboard.
5. Open Onkod settings to choose layout, theme, number row, toolbar, vibration, sound, and long-press delay.
6. Start typing in another Android app.

## Layouts

QWERTY:

```text
1 2 3 4 5 6 7 8 9 0
Q W E R T Y U I O KH
A S D F G H J K L
Shift SH X C DH B N M Backspace
!#1 Globe Somali . Hide
```

ASHERTY:

```text
1 2 3 4 5 6 7 8 9 0
A SH E R T Y U I O KH
Q S D F G H J K L M
Shift W X C DH B N Backspace
!#1 Globe Somali . Hide
```

Symbols:

```text
1 2 3 4 5 6 7 8 9 0
@ # $ % & - + ( ) /
* " ' : ; ! ?
, . _ = < >
ABC Somali Enter Backspace
```

## Somali Rules

- Primary Somali layouts do not show `P`, `V`, or `Z`.
- `P` is replaced by `KH`.
- `V` is replaced by `DH`.
- `Z` is replaced by `SH`.
- `SH`, `DH`, and `KH` commit normal Latin sequences.
- Lowercase commits `sh`, `dh`, and `kh`.
- Shift commits `Sh`, `Dh`, and `Kh`.
- Caps Lock commits `SH`, `DH`, and `KH`.
- The spacebar label is exactly `Somali`.

## Privacy

Onkod Keyboard processes key presses locally. The MVP has no cloud service, no analytics, no advertising, and no account system. Typed text is sent only to the active Android text field through Android's input APIs. Settings are stored locally. Clipboard history is not enabled and clipboard contents are not read automatically.

## Known MVP Limitations

- Android only.
- Emoji panel has a small starter set.
- Clipboard history is intentionally not implemented.
- Somali suggestions and autocorrect are future local-only features.
- The keyboard UI is portrait-first.

## License

A final license has not been selected yet. See `LICENSE`.
