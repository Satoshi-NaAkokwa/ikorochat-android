# 🚀 How to Build & Release the APK

## Prerequisites
- GitHub account
- GitHub token with `repo` and `workflow` scopes

## Quick Build Steps

### 1. Get a GitHub Token
1. Go to https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Set scopes: `repo`, `workflow`
4. Copy the token (e.g., `ghp_xxxxxxxxxxxx`)

### 2. Set the Token
```bash
export GITHUB_TOKEN=*** (Paste your token here)
```

### 3. Trigger the Build
```bash
curl -s -X POST "https://api.github.com/repos/Satoshi-NaAkokwa/ikorochat-android/actions/workflows/259364438/dispatches" \
  -H "Authorization: Bearer ${GITHUB_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  -d '{"ref":"main"}'
```

### 4. Monitor Build
Go to: https://github.com/Satoshi-NaAkokwa/ikorochat-android/actions/workflows/259364438

Wait ~5-10 minutes for completion.

### 5. Download APK
Once build completes, scroll to "Artifacts" and download:
- `Debug-apk.zip` (for testing)
- `Release-apk.zip` (for production)

### 6. Send to Telegram
Upload the APK file in your Telegram chat with the user.

---

## What Was Fixed

### Workflow Files Updated:
1. `android-build.yml` - Primary CI workflow
2. `build-apk.yml` - Simplified build workflow  
3. `build.yml` - Basic build workflow
4. `download-apk.yml` - Download helper workflow

### SDK Configuration:
```yaml
components: ['platform-tools', 'platforms;android-35', 'build-tools;35.0.0', 'build-tools;31.0.0']
```

This matches the project's:
- `compileSdk = 35` (Android 15)
- `targetSdk = 35`
- `minSdk = 26`
