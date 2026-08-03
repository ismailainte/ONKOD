import { useState } from "react";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import { KeyboardPreview } from "@/components/KeyboardPreview";
import { SegmentControl } from "@/components/SegmentControl";
import { LayoutType, ThemeMode } from "@/keyboard/types";

export default function PreviewScreen() {
  const [layout, setLayout] = useState<LayoutType>("qwerty");
  const [theme, setTheme] = useState<Exclude<ThemeMode, "system">>("light");

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Keyboard Preview</Text>
      <Text style={styles.body}>This is a React Native preview. The real system keyboard is the native Android IME.</Text>
      <SegmentControl
        label="Layout"
        value={layout}
        options={[
          { label: "QWERTY", value: "qwerty" },
          { label: "ASHERTY", value: "asherty" }
        ]}
        onChange={setLayout}
      />
      <SegmentControl
        label="Theme"
        value={theme}
        options={[
          { label: "Light", value: "light" },
          { label: "Dark", value: "dark" }
        ]}
        onChange={setTheme}
      />
      <View style={styles.previewWrap}>
        <KeyboardPreview layout={layout} theme={theme} />
      </View>
    </ScrollView>
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
  body: {
    color: "#475569",
    lineHeight: 22
  },
  previewWrap: {
    marginTop: 8
  }
});
