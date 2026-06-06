# Ikoro Wallet Build Status - June 6, 2026

## Current Status

| Item | Status |
|------|--------|
| Repository | https://github.com/Satoshi-NaAkokwa/ikorochat-android |
| Latest Commit | 7652559 "Build config update" |
| APK Location | Not yet built |
| GitHub Actions Workflow | Configured but not triggered |

## Why APK Hasn't Been Built

The GitHub Actions workflow (`build.yml`) is configured to trigger builds on:
1. **Push to main branch** - automatic
2. **Manual trigger via GitHub UI** - required if no new commits

**Current Situation**: The repository is already pushed to GitHub, but the workflow hasn't been triggered. GitHub Actions requires explicit activation via the UI (`workflow_dispatch`).

## Build Process Options

### Option 1: Manual GitHub Actions Trigger (Recommended) ⭐

**Time**: 5-10 minutes
**Steps**:
1. Go to https://github.com/Satoshi-NaAkokwa/ikorochat-android/actions
2. Find **"Build Wallet APK"** workflow
3. Click **"Run workflow"** → **"Run workflow"** button
4. Select `main` branch
5. Wait for build completion (check progress on the same page)
6. Once complete, click on the run → Scroll to **Artifacts** section
7. Download `Ikoro-Wallet-APK.zip`
8. Extract to get `app-debug.apk`

**Verification**:
```bash
# Check downloaded file
ls -lh app-debug.apk
file app-debug.apk
```

### Option 2: Build Locally (Requires Android SDK Setup)

**Prerequisites**: Android SDK with API level 31+

**Steps**:
```bash
cd /root/ikorochat-android
./gradlew assembleDebug --no-daemon
```

**Output**: `app/build/outputs/apk/debug/app-debug.apk`

## Post-Build: Share to Telegram

Once you have the APK file, share it with me and I'll deliver it to your Telegram channel (5622980863).

**Alternative**: Use the helper script once you have the workflow run ID:
```bash
./scripts/download-apk.sh <workflow_run_id>
```

## Quick Commands Reference

### Check GitHub Actions Runs
```bash
cd /root/ikorochat-android
gh run list -L 3
gh run view <run_id>
```

### Download Artifact Manually
1. Go to workflow run page
2. Scroll to Artifacts section
3. Download `Ikoro-Wallet-APK.zip`

## Build Configuration Details

**Workflow File**: `.github/workflows/build.yml`
- Triggers: Push to main, Manual dispatch
- Runtime: Ubuntu 22.04 (ubuntu-latest)
- JDK: 17 (temurin)
- Output: Debug APK in `app/build/outputs/apk/debug/`

**Key Steps**:
1. Checkout code
2. Setup JDK 17
3. Setup Android SDK
4. Build APK with Gradle
5. Upload artifact (7-day retention)

## Troubleshooting

### Build Fails on GitHub Actions
- Check workflow logs for specific errors
- Common issues: dependency resolution, missing SDK components
- Retry the build after fixing any issues locally first

### APK Not Visible in Artifacts
- Wait 5-10 minutes for build completion
- Check workflow run is marked as "Success"
- Refresh the page if artifacts don't appear immediately

### Network Issues Downloading APK
- Try using wget/curl with the artifact URL
- Or download via GitHub web interface

## Next Steps

1. ✅ User manually triggers GitHub Actions build
2. ⏳ Wait for build completion
3. ⏳ Download APK from artifacts
4. ⏳ Share APK with me
5. ⏳ I'll deliver to Telegram channel

---

**Last Updated**: June 6, 2026
**Status**: Awaiting user action to trigger build
# Automation Trigger - Build APK
