# Onkod Keyboard

Onkod is an Android-only native Kotlin system keyboard for Somali, English, and French typing. The launcher and settings screens use Jetpack Compose, while the actual keyboard is an Android `InputMethodService` built with native Android views so it works in other apps.

## Supported Modes

- `SOMALI_QWERTY`
- `ENGLISH_QWERTY`
- `SOMALI_ASHERTY`
- `FRENCH_AZERTY`

## Language Switching

- QWERTY group switches only between Somali and English.
- ASHERTY group switches only between Somali ASHERTY and French AZERTY.
- Tap the globe key to switch the internal language for the selected group.
- Long-press the globe key to open the Android input-method picker.
- QWERTY remembers its last active language independently from ASHERTY.

## Layouts

Somali QWERTY:

```text
1 2 3 4 5 6 7 8 9 0
Q W E R T Y U I O KH
A S D F G H J K L
Shift SH X C DH B N M Backspace
!#1 Globe Somali . Hide
```

English QWERTY:

```text
1 2 3 4 5 6 7 8 9 0
Q W E R T Y U I O P
A S D F G H J K L
Shift Z X C V B N M Backspace
!#1 Globe English . Hide
```

Somali ASHERTY:

```text
1 2 3 4 5 6 7 8 9 0
A SH E R T Y U I O KH
Q S D F G H J K L M
Shift W X C DH B N Backspace
!#1 Globe Somali . Hide
```

French AZERTY:

```text
1 2 3 4 5 6 7 8 9 0
A Z E R T Y U I O P
Q S D F G H J K L M
Shift W X C V B N Backspace
!#1 Globe Français . Hide
```

French long-press accents:

- `A`: `à`, `â`, `æ`
- `C`: `ç`
- `E`: `é`, `è`, `ê`, `ë`
- `I`: `î`, `ï`
- `O`: `ô`, `œ`
- `U`: `ù`, `û`, `ü`
- `Y`: `ÿ`

## Somali Rules

- Somali layouts do not display primary `P`, `V`, or `Z`.
- `P` position is replaced by `KH`.
- `V` position is replaced by `DH`.
- `Z` position is replaced by `SH`.
- `SH`, `DH`, and `KH` commit normal Latin sequences.
- Lowercase commits `sh`, `dh`, `kh`.
- Shift commits `Sh`, `Dh`, `Kh`.
- Caps Lock commits `SH`, `DH`, `KH`.

## Build

```powershell
cd android
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
```

The debug APK is generated at:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

## Install

```powershell
adb install android\app\build\outputs\apk\debug\app-debug.apk
adb shell monkey -p com.onkod.keyboard 1
```

Then enable Onkod in Android keyboard settings and select it from the input-method picker.

## Privacy

Onkod V1 processes typing locally. It does not upload typed text, log typed text, include analytics, show ads, require an account, automatically store clipboard history, or use an accessibility service.

## Known V1 Limitations

- Android only.
- Clipboard history is a placeholder.
- Emoji panel is intentionally small.
- Settings currently use shared native storage so the IME can read them synchronously.
