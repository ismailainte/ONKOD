import { Link } from "expo-router";
import { useEffect, useState } from "react";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import { PrimaryButton } from "@/components/PrimaryButton";
import { SettingRow } from "@/components/SettingRow";
import { OnkodKeyboardNative } from "@/native/OnkodKeyboardNative";

export default function SetupScreen() {
  const [enabled, setEnabled] = useState(false);

  const refreshStatus = async () => {
    setEnabled(await OnkodKeyboardNative.isOnkodKeyboardEnabled());
  };

  useEffect(() => {
    refreshStatus();
  }, []);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Set up Onkod</Text>
      <View style={styles.status}>
        <Text style={styles.statusLabel}>Keyboard enabled</Text>
        <Text style={[styles.statusValue, enabled ? styles.ok : styles.muted]}>
          {enabled ? "Yes" : "Not yet"}
        </Text>
      </View>
      <SettingRow index={1} title="Enable Onkod Keyboard" body="Open Android keyboard settings and enable Onkod." />
      <PrimaryButton label="Open keyboard settings" onPress={OnkodKeyboardNative.openInputMethodSettings} />
      <SettingRow index={2} title="Select Onkod Keyboard" body="Choose Onkod as the current input method." />
      <PrimaryButton label="Show keyboard picker" onPress={OnkodKeyboardNative.showInputMethodPicker} />
      <SettingRow index={3} title="Choose layout" body="Pick QWERTY or ASHERTY in settings." />
      <Link href="/settings" asChild>
        <PrimaryButton label="Open Onkod settings" variant="secondary" />
      </Link>
      <SettingRow index={4} title="Start typing" body="Use Onkod in Messages, WhatsApp, Chrome, Notes, or any text field." />
      <Link href="/privacy" style={styles.link}>
        Privacy notes
      </Link>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 20,
    gap: 14
  },
  title: {
    fontSize: 30,
    fontWeight: "800",
    color: "#111827"
  },
  status: {
    borderWidth: 1,
    borderColor: "#CBD5E1",
    borderRadius: 8,
    padding: 14,
    backgroundColor: "white",
    flexDirection: "row",
    justifyContent: "space-between"
  },
  statusLabel: {
    color: "#334155",
    fontWeight: "700"
  },
  statusValue: {
    fontWeight: "800"
  },
  ok: {
    color: "#047857"
  },
  muted: {
    color: "#64748B"
  },
  link: {
    color: "#0B5FFF",
    fontWeight: "700",
    marginTop: 8
  }
});
