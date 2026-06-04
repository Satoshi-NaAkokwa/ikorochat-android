# Ikoro Wallet - GitHub Build instructions

## Prerequisites

1. **GitHub Personal Access Token (PAT)** with `repo` scope
2. **Java 17** installed locally (optional - for local builds)
3. **Android SDK** with API 35 (optional - for local builds)

## GitHub Actions Build (Recommended)

### Step 1: Push to GitHub

```bash
cd /root/ikorochat-android
git add .
git commit -m "Add wallet module"
git push origin main
```

### Step 2: Enable GitHub Actions Permissions

1. Go to: https://github.com/Satoshi-NaAkokwa/ikorochat-android/settings/actions
2. Under "Workflow permissions":
   - ✅ **Allow GitHub Actions to create and approve pull requests**
   - ✅ **Allow GitHub Actions to create and approve pull requests, approve annotations, and write to the repository**
3. Save

### Step 3: Trigger Build

1. Go to: https://github.com/Satoshi-NaAkokwa/ikorochat-android/actions
2. Select **"Build Wallet APK"** workflow
3. Click **"Run workflow"** → **"Run workflow"**
4. Wait for build to complete (5-10 minutes)

### Step 4: Download APK

1. Go to workflow run page
2. Scroll to **"Artifacts"** section
3. Download **"Ikoro-Wallet-APK"**
4. APK name: `app-release-unsigned.apk`

## Security Features

The wallet includes:

- ✅ **EncryptedSharedPreferences** - AES256-GCM for PIN storage
- ✅ **Android Keystore** - Hardware-backed key storage
- ✅ **Biometric authentication** - Fingerprint/Face unlock
- ✅ **Offline transaction queue** - No server dependency
- ✅ **Mesh broadcast** - Peer-to-peer transaction sync
- ✅ **Fraud detection** - Rate limiting and anomaly detection
- ✅ **No hardcoded secrets** - All credentials externalized

## Build Configuration

### Unsigned Build (for testing)
Works without keystore for initial testing.

### Signed Build (for production)

Create `keystore.properties`:

```properties
storeFile=release.keystore
storePassword=YOUR_PASSWORD
keyAlias=ikoro-wallet
keyPassword=YOUR_PASSWORD
```

Add to `.gitignore`:
```
keystore.properties
release.keystore
```

Generate keystore:
```bash
keytool -genkey -v -keystore release.keystore \
  -alias ikoro-wallet \
  -keyalg RSA -keysize 2048 \
  -validity 10000
```

## Telegram Share Instructions

After building:

1. Download APK from GitHub Actions artifacts
2. Upload to Telegram
3. Send file to: `5622980863`
4. Include version info in message

## Wallet Functionality

### Supported Currencies
- Bitcoin (BTC)
- Stacks OFO
- Naira (NGN)
- USDT (ERC-20/OMNI)
- USDC (ERC-20)

### Features
- Send/Receive transactions
- Offline transaction queue
- PIN authentication (4-6 digits)
- Biometric authentication
- Wallet backup/restore
- Fraud detection alerts
- Mesh-based sync

### Technical Details
- Min SDK: 26 (Android 8.0)
- Target SDK: 35 (Android 15)
- Language: Kotlin
- UI: Jetpack Compose
- Architecture: Modular (separate wallet module)
- Persistence: Room Database
- DI: Hilt

## Security Audit Checklist

Before deployment, verify:

- [ ] No hardcoded API keys
- [ ] No exposed passwords
- [ ] Keystore not in git repo
- [ ] Gradle config empty of passwords
- [ ] GitHub Actions runs in secure environment
- [ ] APK signature verified

## Local Build (Alternative)

```bash
cd /root/ikorochat-android
chmod +x gradlew
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

**Build Status**: Ready for GitHub Actions  
**Security**: No hardcoded secrets  
**Size**: ~15-25MB  
**Testing**: Ready for physical device testing
