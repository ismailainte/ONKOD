# AGENTS.md

## Architecture

- Expo Router app screens live in `app/`.
- Shared TypeScript keyboard data lives in `src/keyboard/`.
- Native React Native bridge code lives in `android/app/src/main/java/com/onkod/keyboard/nativebridge/`.
- Android IME code lives in `android/app/src/main/java/com/onkod/keyboard/ime/`.
- Expo Prebuild IME preservation lives in `plugins/withOnkodKeyboard.js`.

## Commands

- Install: `npm install`
- Typecheck: `npx tsc --noEmit`
- JS tests: `npm test`
- Expo validation: `npx expo-doctor`
- Prebuild: `npx expo prebuild --clean`
- Android tests: `cd android && ./gradlew test`
- Android debug build: `cd android && ./gradlew assembleDebug`
- Windows Gradle: use `gradlew.bat`.

## Coding Conventions

- Use TypeScript for React Native app code.
- Use Kotlin for Android native code.
- Keep keyboard layouts as data models.
- Keep native keyboard behavior out of React Native preview components.
- Keep changes scoped and avoid unrelated refactors.

## Expo Prebuild Rules

- Do not rely on undocumented manual edits to generated Android files.
- Preserve IME manifest, XML configuration, and native templates through `plugins/withOnkodKeyboard.js`.
- Expo Go is not a valid final runtime for this project.

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
