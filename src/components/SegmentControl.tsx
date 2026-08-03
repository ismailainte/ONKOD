import { Pressable, StyleSheet, Text, View } from "react-native";

type Option<T extends string> = {
  label: string;
  value: T;
};

type Props<T extends string> = {
  label: string;
  value: T;
  options: Option<T>[];
  onChange: (value: T) => void;
};

export function SegmentControl<T extends string>({ label, value, options, onChange }: Props<T>) {
  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.segments}>
        {options.map((option) => {
          const selected = option.value === value;
          return (
            <Pressable
              key={option.value}
              accessibilityRole="button"
              accessibilityState={{ selected }}
              onPress={() => onChange(option.value)}
              style={[styles.segment, selected && styles.selected]}
            >
              <Text style={[styles.segmentText, selected && styles.selectedText]}>{option.label}</Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    gap: 8
  },
  label: {
    color: "#334155",
    fontWeight: "800"
  },
  segments: {
    minHeight: 48,
    borderWidth: 1,
    borderColor: "#CBD5E1",
    borderRadius: 8,
    backgroundColor: "white",
    padding: 4,
    flexDirection: "row",
    gap: 4
  },
  segment: {
    flex: 1,
    borderRadius: 6,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 8
  },
  selected: {
    backgroundColor: "#0B5FFF"
  },
  segmentText: {
    color: "#334155",
    fontWeight: "800"
  },
  selectedText: {
    color: "white"
  }
});
