import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { SafeAreaProvider } from "react-native-safe-area-context";

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <StatusBar style="auto" />
      <Stack
        screenOptions={{
          headerStyle: { backgroundColor: "#F8FAFC" },
          headerTitleStyle: { color: "#111827" },
          contentStyle: { backgroundColor: "#F8FAFC" }
        }}
      />
    </SafeAreaProvider>
  );
}
