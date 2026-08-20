# Production Setup Plan - Riva Translate

This plan outlines the steps to prepare the **Riva Translate** application for production deployment. This involves finalizing identification, optimizing the build process, and ensuring security best practices for the release artifact.

## User Review Required

> [!IMPORTANT]
> **Package Name Change**: I will be refactoring `com.example` to `com.rivatranslate`. This is a breaking change for local data (Room database) if you have already deployed the app to a device.
> **Signing Credentials**: I will set up a template for release signing. You will need to provide the actual keystore file and passwords later.

## Proposed Changes

### 1. Identity & Naming
- **Refactor Namespace**: Change `com.example` to `com.rivatranslate` across the entire project.
- **Update Application ID**: Change `com.aistudio.rivatranslate.bcznqd` to `com.rivatranslate.app`.
- **Debug Suffix**: Add `.debug` suffix to the `applicationId` for debug builds to allow side-by-side installation.

### 2. Build Configuration
#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/sande/AndroidStudioProjects/local-translate/app/build.gradle.kts)
- Define a `signingConfigs` block for `release`.
- Use a `secrets.properties` or `keystore.properties` approach to avoid hardcoding sensitive info.
- Finalize `versionCode` and `versionName`.

#### [NEW] [keystore.properties.template](file:///C:/Users/sande/AndroidStudioProjects/local-translate/keystore.properties.template)
- Create a template file for the user to fill in their release keystore details.

### 3. Proguard & R8 Optimization
#### [MODIFY] [app/proguard-rules.pro](file:///C:/Users/sande/AndroidStudioProjects/local-translate/app/proguard-rules.pro)
- Refine existing rules for ML Kit and Room to ensure maximum shrinking without breaking functionality.

### 4. Manifest & Resources
#### [MODIFY] [app/src/main/AndroidManifest.xml](file:///C:/Users/sande/AndroidStudioProjects/local-translate/app/src/main/AndroidManifest.xml)
- Review `android:allowBackup` and `android:fullBackupContent` for data privacy.
- Ensure all permissions are strictly necessary.

---

## Verification Plan

### Automated Tests
- Run `./gradlew assembleRelease` to ensure the build completes successfully with R8 enabled.
- Verify the generated APK's package name and versioning.

### Manual Verification
- Install the release build on a device/emulator and verify that ML Kit translation and Room database operations work correctly with obfuscation enabled.
- Check if the debug and release builds can coexist on the same device.
