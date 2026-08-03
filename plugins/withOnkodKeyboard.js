const { AndroidConfig, withAndroidManifest, withDangerousMod, withStringsXml } = require("expo/config-plugins");
const fs = require("fs");
const path = require("path");

const packageName = "com.onkod.keyboard";
const serviceName = ".ime.OnkodInputMethodService";

const withOnkodKeyboard = (config) => {
  config = withAndroidManifest(config, (mod) => {
    const manifest = mod.modResults;
    const app = AndroidConfig.Manifest.getMainApplicationOrThrow(manifest);
    app.service = app.service ?? [];

    const services = app.service;
    const existing = services.find((service) => service.$?.["android:name"] === serviceName);
    const imeService = existing ?? { $: {} };
    const attributes = imeService.$ ?? {};
    imeService.$ = attributes;
    attributes["android:name"] = serviceName;
    attributes["android:permission"] = "android.permission.BIND_INPUT_METHOD";
    attributes["android:exported"] = "true";
    imeService["intent-filter"] = [
      {
        action: [{ $: { "android:name": "android.view.InputMethod" } }]
      }
    ];
    imeService["meta-data"] = [
      {
        $: {
          "android:name": "android.view.im",
          "android:resource": "@xml/method"
        }
      }
    ];

    if (!existing) {
      services.push(imeService);
    }
    return mod;
  });

  config = withStringsXml(config, (mod) => {
    AndroidConfig.Strings.setStringItem(
      [{ $: { name: "ime_settings_activity" }, _: `${packageName}.MainActivity` }],
      mod.modResults
    );
    return mod;
  });

  config = withDangerousMod(config, [
    "android",
    async (mod) => {
      const templateRoot = path.join(mod.modRequest.projectRoot, "plugins", "onkod-android-template", "app", "src", "main");
      const androidMainRoot = path.join(mod.modRequest.platformProjectRoot, "app", "src", "main");
      copyDirectory(templateRoot, androidMainRoot);
      const testTemplateRoot = path.join(mod.modRequest.projectRoot, "plugins", "onkod-android-template", "app", "src", "test");
      const androidTestRoot = path.join(mod.modRequest.platformProjectRoot, "app", "src", "test");
      copyDirectory(testTemplateRoot, androidTestRoot);

      const xmlDir = path.join(androidMainRoot, "res", "xml");
      fs.mkdirSync(xmlDir, { recursive: true });
      const methodXml = path.join(xmlDir, "method.xml");
      if (!fs.existsSync(methodXml)) {
        fs.writeFileSync(
          methodXml,
          `<?xml version="1.0" encoding="utf-8"?>\n<input-method xmlns:android="http://schemas.android.com/apk/res/android"\n    android:isDefault="false"\n    android:settingsActivity="${packageName}.MainActivity" />\n`
        );
      }

      const appBuildGradle = path.join(mod.modRequest.platformProjectRoot, "app", "build.gradle");
      ensureGradleDependency(appBuildGradle, '    testImplementation("junit:junit:4.13.2")');
      return mod;
    }
  ]);

  return config;
};

function copyDirectory(source, destination) {
  if (!fs.existsSync(source)) return;
  fs.mkdirSync(destination, { recursive: true });
  for (const entry of fs.readdirSync(source, { withFileTypes: true })) {
    if (entry.name === "AndroidManifest.xml") continue;
    const sourcePath = path.join(source, entry.name);
    const destinationPath = path.join(destination, entry.name);
    if (entry.isDirectory()) {
      copyDirectory(sourcePath, destinationPath);
    } else {
      fs.copyFileSync(sourcePath, destinationPath);
    }
  }
}

function ensureGradleDependency(buildGradlePath, dependencyLine) {
  if (!fs.existsSync(buildGradlePath)) return;
  const contents = fs.readFileSync(buildGradlePath, "utf8");
  if (contents.includes(dependencyLine.trim())) return;
  fs.writeFileSync(
    buildGradlePath,
    contents.replace(/dependencies\s*\{/, (match) => `${match}\n${dependencyLine}`)
  );
}

module.exports = withOnkodKeyboard;
