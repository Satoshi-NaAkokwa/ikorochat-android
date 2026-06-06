#!/bin/bash
set -x
cd /root/ikorochat-android

# Update files
git add .github/workflows/*.yml gradle/libs.versions.toml gradle.properties app/build.gradle.kts

# Create commit message
COMMIT_MSG="Fix: APK build configuration - v2

- Removed fetch-depth: 0 (causing git checkout failures)
- Simplified Android SDK setup
- Updated to build DEBUG APK only
- Removed redundant signing config checks
- Updated memory settings and Compose BOM version"

git commit -m "$COMMIT_MSG"

# Push with token
git push https://ghp_dp...5YR7:x-oauth-basic@github.com/Satoshi-NaAkokwa/ikorochat-android.git main
