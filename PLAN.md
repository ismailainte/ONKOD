# Onkod Keyboard MVP Plan

This plan describes the first implementation pass for the revised React Native, Expo, TypeScript, and Android Kotlin MVP.

## 1. Repository And Tooling

- Inspect the repository before implementation.
- If empty, initialize an Expo TypeScript app.
- Use Expo Development Build, not Expo Go.
- Add `expo-dev-client`.
- Use Expo Prebuild for Android project generation.
- Set Android package name to `com.onkod.keyboard`.
- Keep the project Android-first while leaving room for future iOS support.

## 2. Expo App Structure

- Create app routes/screens:
  - Welcome.
  - Setup.
  - Keyboard Preview.
  - Settings.
  - Privacy.
  - About.
- Use TypeScript for app code.
- Keep app UI responsible for onboarding, settings, privacy information, and keyboard preview only.
- Label the React Native keyboard surface as a preview, not the real system keyboard.

## 3. Expo Prebuild Plugin

- Add `plugins/withOnkodKeyboard.js`.
- Make the plugin preserve or add:
  - IME service declaration.
  - `android.permission.BIND_INPUT_METHOD`.
  - `android.view.InputMethod` intent filter.
  - IME metadata reference.
  - `res/xml/method.xml`.
  - Required Android resources.
  - Native Kotlin source placement where appropriate.
- Document that `npx expo prebuild` must not silently remove IME configuration.

## 4. Native Android IME

- Implement `com.onkod.keyboard.ime.OnkodInputMethodService`.
- Build the actual system keyboard using native Kotlin Android views.
- Use Android `InputConnection` for all text input.
- Implement:
  - Normal key commits.
  - Digraph commits.
  - One-time Shift.
  - Caps Lock.
  - Backspace.
  - Long-press Backspace repeat with cancellation.
  - Space.
  - Period.
  - Enter/editor action.
  - Hide Keyboard.
  - Symbols and ABC switching.
  - Globe/input-method switching.
- Ensure the keyboard works inside other Android apps.

## 5. Native Bridge And Shared Settings

- Create a small Android native module for React Native.
- Expose typed methods:
  - `openInputMethodSettings()`.
  - `showInputMethodPicker()`.
  - `isOnkodKeyboardEnabled()`.
  - `getKeyboardSettings()`.
  - `updateKeyboardSettings(settings)`.
- Add TypeScript wrappers for the native module.
- Store settings in one native Android shared store, such as SharedPreferences.
- Have both React Native and the Kotlin IME read the same settings.
- Avoid separate inconsistent settings stores.

## 6. Somali Layout Data

- Define keyboard layouts as data models.
- QWERTY letter rows:
  - `Q W E R T Y U I O KH`
  - `A S D F G H J K L`
  - `SH X C DH B N M`
- ASHERTY letter rows:
  - `A SH E R T Y U I O KH`
  - `Q S D F G H J K L M`
  - `W X C DH B N`
- Symbols layout includes:
  - `1 2 3 4 5 6 7 8 9 0`
  - `@ # SOS % & - + ( ) /`
  - `* " ' : ; ! ?`
  - `, . _ = < >`
  - Backspace, ABC, Space, and Enter or Hide Keyboard.
- Spacebar label must be exactly `Somali`.

## 7. Somali Input Rules

- Do not show primary `P`, `V`, or `Z` keys in Somali layouts.
- Replace `P` with `KH`.
- Replace `V` with `DH`.
- Replace `Z` with `SH`.
- Commit digraph keys as normal Latin sequences.
- Lowercase commits `sh`, `dh`, and `kh`.
- One-time Shift commits `Sh`, `Dh`, and `Kh`.
- Caps Lock commits `SH`, `DH`, and `KH`.
- ASHERTY must include `W`.

## 8. Keyboard UI Requirements

- Keep QWERTY and ASHERTY dimensions identical.
- Match keyboard height, toolbar height, row heights, key spacing, corner radius, font style, and function-key styling.
- Only letter positions may differ.
- Support portrait phone widths responsively.
- Keep `SH`, `DH`, and `KH` readable without clipping.
- Add visible pressed states.
- Add content descriptions and screen-reader labels where practical.
- Do not use a static screenshot or image as the keyboard.

## 9. Toolbar And Panels

- Toolbar buttons:
  - Emoji.
  - Settings.
  - Clipboard.
  - More.
- Emoji panel commits a small safe set of emojis.
- Settings opens the Onkod app settings screen safely from the IME.
- Clipboard shows a privacy-safe placeholder and does not read clipboard contents.
- More panel contains shortcuts for theme, layout, privacy, and about.
- Translate is excluded from initial MVP unless added as a clearly disabled placeholder.

## 10. Themes

- Implement theme setting values:
  - System.
  - Light.
  - Dark.
- Light mode uses light gray keyboard background, white or near-white keys, dark labels, darker function keys, and blue accent.
- Dark mode uses near-black background, dark gray keys, white labels, distinct function-key surfaces, and blue accent.
- System mode follows Android system theme.
- React Native preview and native IME should use matching theme tokens.

## 11. React Native Screens

- Welcome: app name, Somali-focused description, and continue action.
- Setup: enable keyboard, select keyboard, choose layout, and start typing.
- Preview: QWERTY/ASHERTY and Light/Dark toggles with matching visual layout.
- Settings: layout, theme, number row, toolbar, vibration, sound, key preview, and long-press delay.
- Privacy: local processing, no cloud, no analytics, no account, typed text only goes to active field through Android.
- About: app identity, MVP status, and future iOS note.

## 12. Tests

- Add TypeScript tests for:
  - QWERTY key order.
  - ASHERTY key order.
  - `P`, `V`, and `Z` absence.
  - `SH`, `DH`, and `KH` presence.
  - `W` presence in ASHERTY.
  - Spacebar label.
  - Theme setting values.
- Add Kotlin unit tests for:
  - Digraph transformations.
  - Shift behavior.
  - Caps Lock behavior.
  - Backspace state logic.
  - Layout validation.
  - Shared settings parsing.
- Add instrumentation only where reliable in the local environment.

## 13. Validation Commands

- Install dependencies.
- Run TypeScript checks.
- Run lint.
- Run JavaScript or TypeScript tests.
- Run Expo prebuild.
- Build Android debug app.
- Run Kotlin unit tests.
- Inspect generated Android manifest and confirm IME service remains configured.
- Do not report a command as successful unless it actually succeeds.

Expected commands may include:

```bash
npm install
npx expo-doctor
npx tsc --noEmit
npm test
npx expo prebuild --clean
cd android
./gradlew test
./gradlew assembleDebug
```

On Windows, use the equivalent Gradle wrapper command such as `gradlew.bat`.

## 14. Documentation Deliverables

- Create `README.md` with architecture, Expo Development Build requirement, Android prerequisites, installation, prebuild, APK build, keyboard enablement, layout diagrams, privacy notes, known limitations, and future iOS plan.
- Create `AGENTS.md` with architecture overview, build commands, test commands, formatting rules, native-code boundaries, Expo Prebuild rules, Somali layout invariants, and privacy requirements.
- Add a license placeholder unless a real license is selected.

## Current Environment Note

Initial inspection found the workspace empty. Java, Gradle, Node, npm, and Android SDK availability still need to be validated before implementation and builds. If the toolchain is missing, the project can still be scaffolded, but final build validation will require installing or exposing the local Android and Node tooling.
