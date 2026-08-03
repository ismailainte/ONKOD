export type LayoutType = "qwerty" | "asherty";
export type ThemeMode = "system" | "light" | "dark";
export type LongPressDelay = "normal" | "short" | "long";
export type ShiftState = "lowercase" | "shift" | "caps";

export type KeyboardSettings = {
  layout: LayoutType;
  theme: ThemeMode;
  numberRow: boolean;
  toolbar: boolean;
  vibration: boolean;
  sound: boolean;
  keyPreview: boolean;
  longPressDelay: LongPressDelay;
};

export type KeyKind = "text" | "shift" | "backspace" | "symbols" | "globe" | "space" | "period" | "enter" | "hide" | "abc";

export type KeyboardKey = {
  label: string;
  kind: KeyKind;
  weight?: number;
};

export type KeyboardLayout = {
  name: LayoutType;
  toolbar: string[];
  numberRow: string[];
  letterRows: string[][];
  bottomRow: KeyboardKey[];
  spaceLabel: "Somali";
};

export const DEFAULT_SETTINGS: KeyboardSettings = {
  layout: "qwerty",
  theme: "system",
  numberRow: true,
  toolbar: true,
  vibration: true,
  sound: false,
  keyPreview: true,
  longPressDelay: "normal"
};
