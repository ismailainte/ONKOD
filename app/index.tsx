import { Link } from "expo-router";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import { PrimaryButton } from "@/components/PrimaryButton";

export default function WelcomeScreen() {
  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.logo}>
        <Text style={styles.logoText}>O</Text>
      </View>
      <Text style={styles.title}>Onkod Keyboard</Text>
      <Text style={styles.subtitle}>
        A Somali-focused Android system keyboard with QWERTY and ASHERTY layouts.
      </Text>
      <Link href="/setup" asChild>
        <PrimaryButton label="Continue" />
      </Link>
      <Link href="/preview" style={styles.link}>
        Preview keyboard
      </Link>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
    gap: 18
  },
  logo: {
    width: 88,
    height: 88,
    borderRadius: 24,
    backgroundColor: "#0B5FFF",
    alignItems: "center",
    justifyContent: "center"
  },
  logoText: {
    color: "white",
    fontSize: 48,
    fontWeight: "800"
  },
  title: {
    fontSize: 32,
    fontWeight: "800",
    color: "#111827",
    textAlign: "center"
  },
  subtitle: {
    color: "#475569",
    fontSize: 17,
    lineHeight: 24,
    textAlign: "center"
  },
  link: {
    color: "#0B5FFF",
    fontWeight: "700",
    marginTop: 8
  }
});
