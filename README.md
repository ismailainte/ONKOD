# Onkod Keyboard

Onkod Keyboard is an Android-first Somali system keyboard MVP. The app shell is built with React Native, Expo, and TypeScript. The real keyboard is implemented natively in Kotlin with Android `InputMethodService`, because Expo Go cannot register or run an Android system keyboard.

## Architecture

- Expo app: onboarding, setup, keyboard preview, settings, privacy, and about screens.
- Native bridge: Android-only React Native module for opening input-method settings, showing the picker, checking enabled status, and sharing settings.
- Native IME: Kotlin keyboard service and view used inside WhatsApp, Messages, Chrome, Notes, and other Android text fields.
- Expo config plugin: `plugins/withOnkodKeyboard.js` preserves IME manifest, XML configuration, and native Kotlin templates during prebuild.

## Requirements

- Node.js and npm.
- Expo Development Build.
- Android Studio with Android SDK 35 or newer compatible SDK.
- JDK 17.
- Android device or emulator.

Expo Go is not supported for the final runtime.

## Local Development

```bash
npm install
npx expo-doctor
npx tsc --noEmit
npm test
npx expo prebuild --clean
npx expo run:android
```

To build from the generated Android project:

```bash
cd android
./gradlew test
./gradlew assembleDebug
```

On Windows:

```powershell
cd android
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

## Enable The Keyboard

1. Install the development build or debug APK.
2. Open Onkod Keyboard.
3. Tap **Open keyboard settings** and enable Onkod Keyboard.
4. Tap **Show keyboard picker** and select Onkod Keyboard.
5. Choose QWERTY or ASHERTY in settings.
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

- Android only; iOS keyboard extension is future work.
- Emoji panel has a small starter set.
- Clipboard history is intentionally not implemented.
- Somali suggestions and autocorrect are future local-only features.
- The keyboard UI is portrait-first.

## License

A final license has not been selected yet. See `LICENSE`.
