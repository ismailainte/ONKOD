import { StyleSheet, Text, View } from "react-native";
import { getSomaliLayout } from "@/keyboard/layouts";
import { LayoutType, ThemeMode } from "@/keyboard/types";

export function KeyboardPreview({ layout, theme }: { layout: LayoutType; theme: Exclude<ThemeMode, "system"> }) {
  const keyboard = getSomaliLayout(layout);
  const dark = theme === "dark";

  return (
    <View style={[styles.keyboard, dark ? styles.keyboardDark : styles.keyboardLight]}>
      <View style={styles.toolbar}>
        {keyboard.toolbar.map((item) => (
          <View key={item} style={[styles.toolbarButton, dark && styles.functionDark]}>
            <Text style={[styles.functionText, dark && styles.textDark]}>{item}</Text>
          </View>
        ))}
      </View>
      <KeyRow labels={keyboard.numberRow} dark={dark} />
      {keyboard.letterRows.map((row, index) => (
        <KeyRow key={index} labels={row} dark={dark} />
      ))}
      <View style={styles.row}>
        {keyboard.bottomRow.map((key) => (
          <View key={key.label} style={[styles.key, styles.functionKey, { flex: key.weight ?? 1 }, dark && styles.functionDark]}>
            <Text style={[styles.keyText, styles.functionText, dark && styles.textDark]} numberOfLines={1}>
              {key.label === "Globe" ? "🌐" : key.label === "Hide" ? "⌄" : key.label}
            </Text>
          </View>
        ))}
      </View>
    </View>
  );
}

function KeyRow({ labels, dark }: { labels: string[]; dark: boolean }) {
  return (
    <View style={styles.row}>
      {labels.map((label) => (
        <View key={label} style={[styles.key, dark ? styles.keyDark : styles.keyLight]}>
          <Text style={[styles.keyText, dark && styles.textDark]} numberOfLines={1}>
            {label}
          </Text>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  keyboard: {
    borderRadius: 8,
    padding: 6,
    gap: 6
  },
  keyboardLight: {
    backgroundColor: "#D7DCE3"
  },
  keyboardDark: {
    backgroundColor: "#111317"
  },
  toolbar: {
    height: 36,
    flexDirection: "row",
    gap: 6
  },
  toolbarButton: {
    flex: 1,
    borderRadius: 7,
    backgroundColor: "#E5E7EB",
    alignItems: "center",
    justifyContent: "center"
  },
  row: {
    height: 42,
    flexDirection: "row",
    gap: 5
  },
  key: {
    flex: 1,
    borderRadius: 7,
    alignItems: "center",
    justifyContent: "center",
    minWidth: 0
  },
  keyLight: {
    backgroundColor: "#FFFFFF"
  },
  keyDark: {
    backgroundColor: "#2B2F36"
  },
  functionKey: {
    backgroundColor: "#E5E7EB"
  },
  functionDark: {
    backgroundColor: "#3A4048"
  },
  keyText: {
    color: "#111827",
    fontWeight: "800",
    fontSize: 13
  },
  functionText: {
    color: "#334155",
    fontSize: 11
  },
  textDark: {
    color: "#F8FAFC"
  }
});
