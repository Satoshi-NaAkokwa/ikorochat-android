# Ikoro Wallet Android Build Process

## Overview

This skill guides the complete build process for the Ikoro Wallet Android app, troubleshooting common issues and ensuring successful APK generation.

## Prerequisites

- Android SDK with build-tools 34+, compileSdk 35
- JDK 17+ installed
- Gradle 8.13
- Kotlin 2.2.0 compatible

## Common Issues & Solutions

### Issue 1: Compose BOM Version Error

**Problem:** Future-dated Compose BOM version `2025.06.01` doesn't exist or is unstable.

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

### Issue 3: AGP 8.10.1 DSL Incompatibility

**Problem:** Deprecated properties like `enableSplit`, `jvmTarget` cause build failure.

**Solution:** Update `app/build.gradle.kts`:
- Replace `enableSplit` with `isEnable`
- Replace `jvmTarget` with `compilerOptions { jvmTarget.set(...) }`
- Add `import java.util.Properties`

## Build Steps

### Step 1: Fix Configuration Files

Update these files in `/root/ikorochat-android/`:

#### gradle/libs.versions.toml
```toml
[versions]
agp = "8.10.1"
kotlin = "2.2.0"
compileSdk = "35"
minSdk = "26"
targetSdk = "35"
# Fix Compose BOM to stable version
compose-bom = "2024.06.01"
```

#### gradle.properties
```properties
org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=2g -XX:+UseG1GC
```

#### app/build.gradle.kts
Ensure `import java.util.Properties` exists and signing config is properly wrapped.

### Step 2: Clean Gradle Cache
```bash
cd /root/ikorochat-android
./gradlew clean --no-daemon
```

### Step 3: Build Debug APK
```bash
./gradlew assembleDebug --no-daemon --stacktrace
```

### Step 4: Verify APK Output
```bash
find app/build/outputs/apk -name "*.apk" -type f -exec ls -lh {} \;
```

### Step 5: Upload to GitHub Actions
```bash
# Push all changes to trigger CI build
git add .
git commit -m "Fix: build configuration for successful APK generation"
git push origin main
```

## GitHub Actions Workflow

The workflow should include:
1. Checkout code with `fetch-depth: 0`
2. Setup JDK 17
3. Setup Android SDK via `android-actions/setup-android@v3`
4. Gradle build with sufficient memory
5. Upload APK artifact

## Final Verification

After successful build:
1. Download APK from GitHub Actions artifacts
2. Verify APK is installable
3. Test basic functionality
4. Deliver to Telegram channel

## Troubleshooting

If build still fails:
1. Check full Gradle stacktrace for specific errors
2. Verify all dependencies resolve
3. Check for missingresource files
4. Ensure minimum AndroidSdk version is available in CI
