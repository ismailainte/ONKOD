# AGENTS.md

## Architecture

- Native Android app lives under `android/`.
- Kotlin app screens live in `android/app/src/main/java/com/onkod/keyboard/`.
- Android IME code lives in `android/app/src/main/java/com/onkod/keyboard/ime/`.
- Android resources live in `android/app/src/main/res/`.
- Unit tests live in `android/app/src/test/`.

## Commands

- Android tests: `cd android && ./gradlew test`
- Android debug build: `cd android && ./gradlew assembleDebug`
- Windows tests: `cd android; .\gradlew.bat test`
- Windows debug build: `cd android; .\gradlew.bat assembleDebug`

## Coding Conventions

- Use Kotlin for app screens and IME code.
- Build keyboard UI with native Android views.
- Keep keyboard layouts as data models.
- Keep settings in `SettingsStore`.
- Do not reintroduce React Native, Expo, Flutter, WebView, analytics, ads, or networking for MVP typing behavior.

## Somali Layout Invariants

- No primary `P`, `V`, or `Z` keys in Somali layouts.
- `P` is replaced by `KH`.
- `V` is replaced by `DH`.
- `Z` is replaced by `SH`.
- QWERTY and ASHERTY visual dimensions must match.
- ASHERTY must contain `W`.
- Spacebar text must be exactly `Somali`.
- `SH`, `DH`, and `KH` commit normal Latin sequences.

## Privacy Requirements

- Never log typed text.
- Do not add analytics, advertising, accounts, networking, or accessibility services.
- Do not read clipboard contents automatically.
- Store settings locally.
