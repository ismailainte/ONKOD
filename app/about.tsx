import { ScrollView, StyleSheet, Text } from "react-native";

export default function AboutScreen() {
  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>About Onkod</Text>
      <Text style={styles.body}>Onkod Keyboard is an Android-first Somali keyboard MVP built with Expo, React Native, TypeScript, and native Kotlin.</Text>
      <Text style={styles.body}>The Android keyboard is a native InputMethodService. The Expo app provides onboarding, preview, settings, privacy, and about screens.</Text>
      <Text style={styles.body}>Future iOS support can be added with a separate iOS keyboard extension.</Text>
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
