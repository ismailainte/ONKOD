# Onkod Keyboard Kotlin Plan

This plan tracks the Kotlin-first direction for Onkod Keyboard.

## 1. Project Structure

- Keep `android/` as the runnable native Android project.
- Use Kotlin and Android Gradle Plugin.
- Package name remains `com.onkod.keyboard`.
- Minimum SDK remains API 26.
- Target SDK remains API 35.

## 2. Native App Screens

- `MainActivity` provides onboarding and setup.
- `SettingsActivity` provides layout, theme, number row, toolbar, vibration, sound, key preview, and long-press delay controls.
- App screens use native Android views to avoid extra dependencies.

## 3. Native IME

- `OnkodInputMethodService` owns Android input behavior.
- `OnkodKeyboardView` renders the keyboard with native Android views.
- `SettingsStore` shares local settings between the app screens and IME.
- `KeyboardLayouts` defines QWERTY, ASHERTY, and symbols layouts as data.

## 4. Somali Invariants

- QWERTY and ASHERTY must not show primary `P`, `V`, or `Z`.
- `KH`, `DH`, and `SH` are visible single keys.
- `KH`, `DH`, and `SH` commit two normal Latin characters.
- ASHERTY must include `W`.
- Spacebar label must be exactly `Somali`.

## 5. Validation

- Run `cd android && ./gradlew test`.
- Run `cd android && ./gradlew assembleDebug`.
- Confirm manifest includes `OnkodInputMethodService`, `BIND_INPUT_METHOD`, `android.view.InputMethod`, and `@xml/method`.
- Confirm no unnecessary permissions are present.
- Confirm typed text is not logged.

## Current Environment Note

This machine still does not expose Java/JDK on `PATH`, so Gradle commands cannot complete until JDK 17 is installed and `JAVA_HOME` is set.
