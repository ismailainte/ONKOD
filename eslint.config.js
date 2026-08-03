const js = require("@eslint/js");
const tseslint = require("typescript-eslint");

module.exports = [
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ["**/*.ts", "**/*.tsx"],
    languageOptions: {
      globals: {
        console: "readonly",
        describe: "readonly",
        expect: "readonly",
        jest: "readonly",
        test: "readonly"
      }
    },
    rules: {
      "no-unused-vars": "off"
    }
  },
  {
    ignores: ["node_modules/", ".expo/", "android/build/", "android/app/build/", "babel.config.js", "eslint.config.js", "plugins/**/*.js"]
  }
];
