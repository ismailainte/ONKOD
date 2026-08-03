import { forwardRef, type ElementRef } from "react";
import { Pressable, StyleSheet, Text } from "react-native";

type Props = {
  label: string;
  variant?: "primary" | "secondary";
  onPress?: () => void;
};

export const PrimaryButton = forwardRef<ElementRef<typeof Pressable>, Props>(
  ({ label, variant = "primary", onPress }, ref) => (
    <Pressable
      ref={ref}
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [
        styles.button,
        variant === "secondary" && styles.secondary,
        pressed && styles.pressed
      ]}
    >
      <Text style={[styles.label, variant === "secondary" && styles.secondaryLabel]}>{label}</Text>
    </Pressable>
  )
);

const styles = StyleSheet.create({
  button: {
    width: "100%",
    minHeight: 52,
    borderRadius: 8,
    backgroundColor: "#0B5FFF",
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 16
  },
  secondary: {
    backgroundColor: "white",
    borderWidth: 1,
    borderColor: "#0B5FFF"
  },
  pressed: {
    opacity: 0.78
  },
  label: {
    color: "white",
    fontWeight: "800",
    fontSize: 16
  },
  secondaryLabel: {
    color: "#0B5FFF"
  }
});
