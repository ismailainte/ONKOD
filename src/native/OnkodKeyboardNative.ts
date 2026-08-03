import { NativeModules, Platform } from "react-native";
import { DEFAULT_SETTINGS, KeyboardSettings } from "@/keyboard/types";

type NativeOnkodKeyboardModule = {
  openInputMethodSettings: () => Promise<void>;
  showInputMethodPicker: () => Promise<void>;
  isOnkodKeyboardEnabled: () => Promise<boolean>;
  getKeyboardSettings: () => Promise<KeyboardSettings>;
  updateKeyboardSettings: (settings: KeyboardSettings) => Promise<KeyboardSettings>;
};

const nativeModule = NativeModules.OnkodKeyboard as NativeOnkodKeyboardModule | undefined;

const unsupported: NativeOnkodKeyboardModule = {
  openInputMethodSettings: async () => undefined,
  showInputMethodPicker: async () => undefined,
  isOnkodKeyboardEnabled: async () => false,
  getKeyboardSettings: async () => DEFAULT_SETTINGS,
  updateKeyboardSettings: async (settings) => settings
};

export const OnkodKeyboardNative: NativeOnkodKeyboardModule =
  Platform.OS === "android" && nativeModule ? nativeModule : unsupported;
