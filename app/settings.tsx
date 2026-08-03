import { useEffect, useState } from "react";
import { ScrollView, StyleSheet, Switch, Text, View } from "react-native";
import { SegmentControl } from "@/components/SegmentControl";
import { DEFAULT_SETTINGS, KeyboardSettings, LayoutType, LongPressDelay, ThemeMode } from "@/keyboard/types";
import { OnkodKeyboardNative } from "@/native/OnkodKeyboardNative";

export default function SettingsScreen() {
  const [settings, setSettings] = useState<KeyboardSettings>(DEFAULT_SETTINGS);

  useEffect(() => {
    OnkodKeyboardNative.getKeyboardSettings().then(setSettings);
  }, []);

  const update = async (patch: Partial<KeyboardSettings>) => {
    const next = { ...settings, ...patch };
    setSettings(next);
    await OnkodKeyboardNative.updateKeyboardSettings(next);
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Settings</Text>
      <SegmentControl<LayoutType>
        label="Layout"
        value={settings.layout}
        options={[
          { label: "QWERTY", value: "qwerty" },
          { label: "ASHERTY", value: "asherty" }
        ]}
        onChange={(layout) => update({ layout })}
      />
      <SegmentControl<ThemeMode>
        label="Theme"
        value={settings.theme}
        options={[
          { label: "System", value: "system" },
          { label: "Light", value: "light" },
          { label: "Dark", value: "dark" }
        ]}
        onChange={(theme) => update({ theme })}
      />
      <SegmentControl<LongPressDelay>
        label="Long-press delay"
        value={settings.longPressDelay}
        options={[
          { label: "Normal", value: "normal" },
          { label: "Short", value: "short" },
          { label: "Long", value: "long" }
        ]}
        onChange={(longPressDelay) => update({ longPressDelay })}
      />
      <Toggle label="Number row" value={settings.numberRow} onValueChange={(numberRow) => update({ numberRow })} />
      <Toggle label="Toolbar" value={settings.toolbar} onValueChange={(toolbar) => update({ toolbar })} />
      <Toggle label="Key vibration" value={settings.vibration} onValueChange={(vibration) => update({ vibration })} />
      <Toggle label="Key sound" value={settings.sound} onValueChange={(sound) => update({ sound })} />
      <Toggle label="Show key preview" value={settings.keyPreview} onValueChange={(keyPreview) => update({ keyPreview })} />
    </ScrollView>
  );
}

function Toggle({ label, value, onValueChange }: { label: string; value: boolean; onValueChange: (value: boolean) => void }) {
  return (
    <View style={styles.toggle}>
      <Text style={styles.toggleText}>{label}</Text>
      <Switch value={value} onValueChange={onValueChange} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 20,
    gap: 18
  },
  title: {
    fontSize: 28,
    fontWeight: "800",
    color: "#111827"
  },
  toggle: {
    minHeight: 56,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: "#CBD5E1",
    backgroundColor: "white",
    paddingHorizontal: 14,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between"
  },
  toggleText: {
    color: "#111827",
    fontWeight: "700"
  }
});
