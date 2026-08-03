# Onkod Keyboard Roadmap

Onkod Keyboard is now a native Android Kotlin project. The priority is a real Somali system keyboard that installs as an Android IME and works inside other apps.

## Phase 1: Kotlin MVP

- Keep the app Kotlin-only.
- Implement native onboarding and settings screens.
- Implement `InputMethodService` for the actual keyboard.
- Support Somali QWERTY, ASHERTY, and symbols layouts.
- Enforce no primary `P`, `V`, or `Z` keys.
- Commit `SH`, `DH`, and `KH` as normal Latin digraphs.
- Support Shift, Caps Lock, Backspace repeat, Space, Period, Enter, Hide Keyboard, Globe switching, toolbar, and emoji starter panel.
- Persist settings locally with SharedPreferences.
- Add local unit tests for layout and settings invariants.

## Phase 2: Typing Polish

- Tune responsive phone-width sizing.
- Improve pressed states and Shift indicators.
- Refine haptics, optional sound, and Backspace repeat acceleration.
- Improve accessibility labels and font scaling.
- Expand emoji panel safely.

## Phase 3: Somali Language Features

- Add optional local-only Somali suggestions.
- Add optional autocapitalization.
- Add optional double-space period.
- Add offline dictionary support.

## Phase 4: Release Readiness

- Add production icon assets.
- Add release signing documentation.
- Test on multiple Android versions and OEM keyboards.
- Add CI once the Android toolchain is available.
- Choose a real license.

## Non-Goals For MVP

- No React Native or Expo runtime.
- No iOS keyboard extension.
- No networking, analytics, advertising, or account system.
- No server-side typing processing.
- No clipboard history or automatic clipboard reading.
- No accessibility service.
