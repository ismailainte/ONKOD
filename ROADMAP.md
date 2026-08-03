# Onkod Keyboard Roadmap

Onkod Keyboard is a Somali Android system keyboard built with React Native, Expo, TypeScript, and a native Android Kotlin IME. Expo provides the app experience for onboarding, settings, privacy, and previews. Kotlin provides the actual `InputMethodService`, because Android system keyboards cannot run from Expo Go alone.

## Phase 1: Expo + Native IME MVP

Goal: ship a runnable Android development build that users can enable as a system keyboard and use in other apps.

- Create an Expo React Native TypeScript project using Expo Development Build.
- Use Expo Prebuild and a local config plugin at `plugins/withOnkodKeyboard.js`.
- Configure package name `com.onkod.keyboard`.
- Implement React Native screens for Welcome, Setup, Keyboard Preview, Settings, Privacy, and About.
- Implement a native Kotlin `InputMethodService` at `com.onkod.keyboard.ime.OnkodInputMethodService`.
- Add a native React Native bridge for keyboard setup actions and shared settings.
- Persist settings in one Android-native store shared by React Native and Kotlin.
- Build Somali QWERTY, Somali ASHERTY, and Symbols layouts as data models.
- Enforce Somali layout invariants: no primary `P`, `V`, or `Z`; use `KH`, `DH`, and `SH`.
- Implement Shift, Caps Lock, Backspace repeat, Space, Period, Enter, Hide Keyboard, Globe switching, toolbar panels, and emoji MVP.
- Support System, Light, and Dark themes.
- Add TypeScript and Kotlin tests for layout and state behavior.
- Document build, prebuild, install, enablement, privacy, and known limitations.

## Phase 2: Typing Experience Polish

Goal: make the keyboard feel consistent, accessible, and comfortable across Android phones.

- Tune native key sizing for portrait phone widths.
- Keep QWERTY and ASHERTY visually identical except for letter positions.
- Improve two-letter key rendering for `SH`, `DH`, and `KH`.
- Refine pressed states, Shift indicators, haptics, and optional key sounds.
- Improve Backspace repeat timing and acceleration.
- Expand emoji panel safely without reading private data.
- Improve accessibility labels, contrast, and font scaling.
- Add broader test coverage for shared settings and keyboard state transitions.

## Phase 3: Somali Language Features

Goal: add privacy-preserving Somali typing assistance after the core IME is stable.

- Add optional local-only Somali suggestions.
- Add optional autocapitalization.
- Add optional double-space period.
- Add local punctuation spacing improvements.
- Explore an offline custom dictionary.
- Keep all smart typing features transparent and configurable.

## Phase 4: Release Readiness

Goal: prepare for wider testing and distribution.

- Add production app icon and adaptive icon assets.
- Add release build and signing documentation.
- Add CI for TypeScript checks, tests, Expo validation, and Android builds.
- Test across Android versions and OEM keyboard settings.
- Add Play Store privacy/data safety documentation.
- Choose and add a real license.
- Prepare a future iOS plan, without implementing the iOS keyboard extension in the MVP.

## Non-Goals For MVP

- No Expo Go runtime as the final target.
- No iOS keyboard extension.
- No networking, analytics, advertising, or account system.
- No server-side typing processing.
- No clipboard history or automatic clipboard reading.
- No accessibility service.
- No custom Unicode characters for Somali digraphs.
