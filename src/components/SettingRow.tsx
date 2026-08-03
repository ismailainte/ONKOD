import { StyleSheet, Text, View } from "react-native";

export function SettingRow({ index, title, body }: { index: number; title: string; body: string }) {
  return (
    <View style={styles.row}>
      <View style={styles.badge}>
        <Text style={styles.badgeText}>{index}</Text>
      </View>
      <View style={styles.content}>
        <Text style={styles.title}>{title}</Text>
        <Text style={styles.body}>{body}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    gap: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: "#CBD5E1",
    borderRadius: 8,
    backgroundColor: "white"
  },
  badge: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: "#DBEAFE",
    alignItems: "center",
    justifyContent: "center"
  },
  badgeText: {
    color: "#0B5FFF",
    fontWeight: "800"
  },
  content: {
    flex: 1,
    gap: 4
  },
  title: {
    color: "#111827",
    fontWeight: "800"
  },
  body: {
    color: "#475569",
    lineHeight: 20
  }
});
