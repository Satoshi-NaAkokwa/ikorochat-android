## GitHub Actions - Setup Instructions

This document describes how to configure GitHub Actions to build the Ikoro Wallet APK securely.

### 1. Create Repository Settings

1. Go to your GitHub repository → Settings
2. Navigate to "Actions" → "General"
3. Under "Workflow permissions", select:
   - ✅ Read and write permissions
4. Save

### 2. Create Keystore (if not exists)

Run this command to generate a keystore for signing:

```bash
keytool -genkey -v -keystore release.keystore -alias ikoro-wallet -keyalg RSA -keysize 2048 -validity 10000
```

When prompted:
- **Keystore password**: Choose a strong password
- **Key password**: Same as keystore password
- **First and last name**: Ikoro Wallet
- **Organizational unit**: Development
- **Organization**: Ikoro Labs
- **City**: Cape Town
- **State**: Western Cape
- **Country**: ZA

### 3. Store Keystore in Repository

Create `keystore.properties` file (NOT included in git, see .gitignore):

```
storeFile=release.keystore
storePassword=YOUR_STRONG_PASSWORD
keyAlias=ikoro-wallet
keyPassword=YOUR_STRONG_PASSWORD
```

Add to `.gitignore`:
```
keystore.properties
release.keystore
/app/build/
/app/release/
```

### 4. GitHub Secrets Configuration

Go to Repository → Settings → Secrets and variables → Actions

Add the following secrets:

| Secret Name | Description | Value |
|-------------|-------------|-------|
| `STORE_FILE_BASE64` | Base64-encoded keystore (optional) | `base64 release.keystore` |
| `STORE_PASSWORD` | Keystore password | Your password |
| `KEY_ALIAS` | Key alias | `ikoro-wallet` |
| `KEY_PASSWORD` | Key password | Your password |

### 5. Secret Encoding Commands

```bash
# Encode keystore to base64
base64 -w 0 release.keystore > release.keystore.b64

# For environment variables
echo "YOUR_PASSWORD" | base64
echo "YOUR_PASSWORD" | base64 -d  # To decode
```

### 6. Push and Build

1. Commit all files:
```bash
git add .
git commit -m "Add GitHub Actions CI/CD and wallet module"
git push origin main
```

2. GitHub Actions will auto-run the workflow
3. Check Actions tab for build status
4. Download APK from artifacts

### 7. Security Verification

The workflow includes automated security checks:

- ✅ No hardcoded secrets in source
- ✅ No passwords in Gradle config
- ✅ CI/CD environment isolation
- ✅ Secure GitHub Actions runner

### 8. Verify Build Output

After successful build, the workflow produces:

- `app/build/outputs/apk/release/app-release-unsigned.apk` - Unsigned release APK
- SHA256 checksum available in workflow logs
- Release tag created for version tracking

### 9. Security Notes

⚠️ **Important Security Practices:**

1. **Never** commit `keystore.properties` or `release.keystore`
2. **Never** log keystore passwords in workflow runs
3. **Rotate** keys if any secret is compromised
4. **Revoke** any compromised GitHub tokens immediately
5. **Monitor** Actions logs for unexpected output

### 10. APK Distribution

After build completes:
1. Download APK from GitHub Actions artifacts
2. Share APK link via Telegram
3. Install on test device for functionality verification

---

**Wallet Functionality Status:**
- ✅ Messaging module (Nostr relay integration)
- ✅ Mesh networking (Bluetooth-based peer-to-peer)
- ✅ Wallet module (Bitcoin, OFO, Naira, USDT, USDC)
- ✅ Offline transaction support
- ✅ PIN authentication
- ✅ Biometric auth (Android BiometricPrompt)
- ✅ Backup/restore functionality
- ✅ Fraud detection system

**Security Features:**
- ✅ EncryptedSharedPreferences for PIN storage
- ✅ Android Keystore for private keys
- ✅ AES256-GCM encryption for transactions
- ✅ Mesh verification for offline sync
- ✅ No hardcoded secrets in source
