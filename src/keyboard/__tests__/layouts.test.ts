import { keyOutput, somaliAshertyLayout, somaliQwertyLayout, themeValues, validateSomaliLayout } from "../layouts";

describe("Somali keyboard layouts", () => {
  test("QWERTY key order is fixed", () => {
    expect(somaliQwertyLayout.letterRows).toEqual([
      ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "KH"],
      ["A", "S", "D", "F", "G", "H", "J", "K", "L"],
      ["SH", "X", "C", "DH", "B", "N", "M"]
    ]);
  });

  test("ASHERTY key order is fixed and includes W", () => {
    expect(somaliAshertyLayout.letterRows).toEqual([
      ["A", "SH", "E", "R", "T", "Y", "U", "I", "O", "KH"],
      ["Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"],
      ["W", "X", "C", "DH", "B", "N"]
    ]);
  });

  test("layouts exclude P, V, and Z primary keys", () => {
    const labels = [...somaliQwertyLayout.letterRows.flat(), ...somaliAshertyLayout.letterRows.flat()];
    expect(labels).not.toContain("P");
    expect(labels).not.toContain("V");
    expect(labels).not.toContain("Z");
  });

  test("digraph keys transform by shift state", () => {
    expect(keyOutput("SH", "lowercase")).toBe("sh");
    expect(keyOutput("DH", "shift")).toBe("Dh");
    expect(keyOutput("KH", "caps")).toBe("KH");
  });

  test("spacebar and theme invariants are valid", () => {
    expect(somaliQwertyLayout.spaceLabel).toBe("Somali");
    expect(somaliAshertyLayout.spaceLabel).toBe("Somali");
    expect(themeValues).toEqual(["system", "light", "dark"]);
    expect(validateSomaliLayout(somaliQwertyLayout)).toEqual([]);
    expect(validateSomaliLayout(somaliAshertyLayout)).toEqual([]);
  });
});
