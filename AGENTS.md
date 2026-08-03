# AGENTS.md

## Architecture

- Native Android project lives under `android/`.
- Compose launcher/settings screens live in `android/app/src/main/java/com/onkod/keyboard/`.
- IME code lives in `android/app/src/main/java/com/onkod/keyboard/ime/`.
- Keyboard layouts are data-driven in `KeyboardLayout.kt`.
- Unit tests live in `android/app/src/test/`.

## Commands

- Test: `cd android && ./gradlew test`
- Lint: `cd android && ./gradlew lint`
- Build: `cd android && ./gradlew assembleDebug`
- Windows: use `gradlew.bat`.

## Coding Rules

- Kotlin only.
- Jetpack Compose for launcher/settings/preview app surfaces.
- Native Android views are acceptable for the `InputMethodService` keyboard UI.
- Do not add React Native, Expo, Flutter, WebView, JavaScript, analytics, ads, accounts, or networking.
- Never log typed text.

## Layout Invariants

- Somali layouts do not display `P`, `V`, or `Z`.
- `P` is replaced by `KH` in Somali layouts.
- `V` is replaced by `DH` in Somali layouts.
- `Z` is replaced by `SH` in Somali layouts.
- English QWERTY retains `P`, `V`, and `Z`.
- French AZERTY retains `P`, `V`, and `Z`.
- Somali ASHERTY includes `W`.
- French AZERTY begins with `A Z E R T Y`.
- QWERTY switches only between Somali and English.
- ASHERTY switches only between Somali and French.
- Spacebar labels are exactly `Somali`, `English`, or `Français`.

## Privacy Rules

- Typed text must never be logged or uploaded.
- Do not read clipboard contents automatically.
- Store settings locally.
