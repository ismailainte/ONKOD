import { KeyboardLayout, KeyboardSettings, KeyboardKey, LayoutType, ShiftState, ThemeMode } from "./types";

const numberRow = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"];
const toolbar = ["Emoji", "Settings", "Clipboard", "More"];

const bottomRow: KeyboardKey[] = [
  { label: "!#1", kind: "symbols", weight: 1 },
  { label: "Globe", kind: "globe", weight: 1 },
  { label: "Somali", kind: "space", weight: 4 },
  { label: ".", kind: "period", weight: 1 },
  { label: "Hide", kind: "hide", weight: 1.2 }
];

export const somaliQwertyLayout: KeyboardLayout = {
  name: "qwerty",
  toolbar,
  numberRow,
  letterRows: [
    ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "KH"],
    ["A", "S", "D", "F", "G", "H", "J", "K", "L"],
    ["SH", "X", "C", "DH", "B", "N", "M"]
  ],
  bottomRow,
  spaceLabel: "Somali"
};

export const somaliAshertyLayout: KeyboardLayout = {
  name: "asherty",
  toolbar,
  numberRow,
  letterRows: [
    ["A", "SH", "E", "R", "T", "Y", "U", "I", "O", "KH"],
    ["Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"],
    ["W", "X", "C", "DH", "B", "N"]
  ],
  bottomRow,
  spaceLabel: "Somali"
};

export const symbolsRows = [
  ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"],
  ["@", "#", "$", "%", "&", "-", "+", "(", ")", "/"],
  ["*", "\"", "'", ":", ";", "!", "?"],
  [",", ".", "_", "=", "<", ">"]
];

export const themeValues: ThemeMode[] = ["system", "light", "dark"];

export function getSomaliLayout(type: LayoutType): KeyboardLayout {
  return type === "asherty" ? somaliAshertyLayout : somaliQwertyLayout;
}

export function getLayoutFromSettings(settings: KeyboardSettings): KeyboardLayout {
  return getSomaliLayout(settings.layout);
}

export function keyOutput(label: string, shiftState: ShiftState): string {
  const lower = label.toLowerCase();
  if (shiftState === "caps") {
    return label.toUpperCase();
  }
  if (shiftState === "shift") {
    return lower.charAt(0).toUpperCase() + lower.slice(1);
  }
  return lower;
}

export function validateSomaliLayout(layout: KeyboardLayout): string[] {
  const labels = layout.letterRows.flat();
  const errors: string[] = [];
  for (const blocked of ["P", "V", "Z"]) {
    if (labels.includes(blocked)) {
      errors.push(`${blocked} must not be a primary Somali key`);
    }
  }
  for (const required of ["SH", "DH", "KH"]) {
    if (!labels.includes(required)) {
      errors.push(`${required} is required`);
    }
  }
  if (layout.name === "asherty" && !labels.includes("W")) {
    errors.push("ASHERTY must include W");
  }
  if (layout.spaceLabel !== "Somali") {
    errors.push("Spacebar label must be Somali");
  }
  return errors;
}
