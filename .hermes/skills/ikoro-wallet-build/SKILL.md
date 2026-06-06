---
name: ikoro-wallet-build
category: devops
---

# Ikoro Wallet Android Build Process

## Overview

This skill guides the complete build process for the Ikoro Wallet Android app, troubleshooting common issues and ensuring successful APK generation.

## Prerequisites

- Android SDK with build-tools 34+, compileSdk 35  
- JDK 17+
- Gradle 8.13
- Kotlin 2.2.0 compatible

## Common Issues & Solutions

### Issue 1: Compose BOM Version Error

**Problem:** Future-dated Compose BOM version doesn't exist or is unstable.

**Solution:**
```bash
# Update gradle/libs.versions.toml
compose-bom = "2024.06.01"
```

### Issue 2: Gradle OutOfMemoryError

**Problem:** Insufficient metaspace memory causes Kotlin compilation failure.

**Solution:**
```bash
# Update gradle.properties
org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=2g -XX:+UseG1GC
```

## Build Steps

### Step 1: Update Configuration Files

Update these files in `/root/ikorochat-android/`:

#### gradle/libs.versions.toml
```toml
[versions]
agp = "8.10.1"
kotlin = "2.2.0"
compileSdk = "35"
minSdk = "26"
targetSdk = "35"
compose-bom = "2024.06.01"
```

#### gradle.properties
```properties
org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=2g -XX:+UseG1GC
```

### Step 2: Update GitHub Actions Workflow

#### .github/workflows/build-apk.yml
```yaml
name: Build Wallet APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Build APK (DEBUG)
        run: |
          echo "=== Building APK ==="
          ./gradlew assembleDebug --no-daemon

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: Ikoro-Wallet-APK
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 7
```

### Step 3: Commit and Push

```bash
cd /root/ikorochat-android
git add .
git commit -m "Fix: APK build configuration"
git push origin main
```

### Step 4: Monitor Build

- Go to: https://github.com/Satoshi-NaAkokwa/ikorochat-android/actions
- Click on the latest "Build Wallet APK" run
- Monitor for completion

### Step 5: Download APK

Once build succeeds:
1. Go to: https://github.com/Satoshi-NaAkokwa/ikorochat-android/actions
2. Click on successful "Build Wallet APK" run
3. Scroll to "Artifacts" section
4. Click on "Ikoro-Wallet-APK"
5. Download and install the APK

## Troubleshooting

If build fails:
1. Check full Gradle stacktrace in GitHub Actions logs
2. Verify all dependencies resolve
3. Ensure minimum Android SDK version is available in CI
4. Check for missing resource files

## Notes

- Debug builds don't require signing (no `keystore.properties` needed)
- Release builds require `keystore.properties` file with signing config
- AGP 8.10.1 + Kotlin 2.2.0 + Compose BOM 2024.06.01 is the stable combination
