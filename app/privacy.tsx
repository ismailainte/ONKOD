import { ScrollView, StyleSheet, Text } from "react-native";

export default function PrivacyScreen() {
  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Privacy</Text>
      <Text style={styles.body}>Onkod Keyboard processes key presses locally on your device.</Text>
      <Text style={styles.body}>The MVP has no cloud service, no account system, no analytics, no advertising, and no network features.</Text>
      <Text style={styles.body}>Typed text is sent only to the active Android text field through the operating system.</Text>
      <Text style={styles.body}>Settings are stored locally. Clipboard history is not enabled, and the keyboard does not automatically read clipboard contents.</Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 20,
    gap: 14
  },
  title: {
    fontSize: 28,
    fontWeight: "800",
    color: "#111827"
  },
  body: {
    color: "#334155",
    fontSize: 16,
    lineHeight: 24
  }
});
