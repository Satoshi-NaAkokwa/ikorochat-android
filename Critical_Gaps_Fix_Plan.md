═══════════════════════════════════════════════════════════════
IKORO WALLET - CRITICAL GAPS FIX PLAN
Serverless, No External Backend Required
═══════════════════════════════════════════════════════════════

## CURRENT STATE ASSESSMENT

### ✅ Existing Infrastructure Ready:
1. **Nostr Client** - Full NIP-01/NIP-05/NIP-04/NIP-17 implementation
   - Relay management (NostrRelayManager)
   - Identity management (NostrIdentityBridge)
   - Private messaging via gift wraps (NIP-17)
   - Geohash channel messaging (NIP-01)
   - Proof-of-Work validation
   - Noise protocol encryption

2. **Mesh Networking** - Bluetooth/Low-energy peer-to-peer
   - PeerManager (centralized peer tracking)
   - Peer fingerprint verification
   - Direct vs routed connection support
   - Stale peer cleanup (3-minute timeout)
   - RSSI tracking for signal strength

3. **Wallet Module** - Local-first architecture
   - Room Database (WalletEntity, Transaction)
   - Hilt DI (modular architecture)
   - Jetpack Compose UI
   - KeyManager with Android Keystore
   - EncryptedSharedPreferences for PIN

4. **Security Layer**
   - AES256-GCM encryption
   - Android BiometricPrompt
   - Fraud detection thresholds
   - Key rotation logic

### ❌ MISSING CRITICAL ITEMS (What Prevents Functional Wallet):

## PLAN TO FIX CRITICAL GAPS (Serverless Approach)

### 1. REAL BACKEND CONNECTION - FIXED VIA NOSTR + LOCAL AGENT

**Problem:** AgbaraWallet API needs connection but no server available

**Solution:** AGARABAWALLET LOCAL AGENT PATTERN
- AgbaraWallet v52.11.3 is TypeScript/Node.js-based (backend exists)
- Implementation: Run AgbaraWallet as LOCAL embedded service
- OR: Connect to AgbaraWallet backend via Nostr NIP-17 (peer-to-peer)

**Serverless Implementation Strategy:**
```
┌─────────────────┐        Nostr NIP-17 (encrypted DM)         ┌──────────────────┐
│  ikorochat App  │  ←───────────────────────────────────────> │  AgbaraWallet App│
│  (this wallet)  │                gift wraps                   │ (backend peer)   │
└─────────────────┘                                              └──────────────────┘
         │                                                              │
         │ Nostr Client (NIP-01/05)                                    │ Nostr Client
         │ Local identity: npub1xxyy...                                │ Remote identity
         │                                                             │ npub1zzaa... 
         ▼                                                             ▼
   ┌─────────────────┐                                        ┌──────────────────┐
   │ NostrRelayPool  │                Shared Relays          │ NostrRelayPool   │
   │ (relay manager) │  ←─────────────────────────────────────> │ (relay manager)  │
   └─────────────────┘                                        └──────────────────┘
        │                                                             │
        ▼                                                             ▼
   Public relays: nsec1... (user's personal relays)              nsec1... (backend's)
   Private relays: user-configured relay list                     user-configured
```

**Implementation Steps:**
1. ✅ Create Nostr message handler for wallet commands (WALLET_REQUEST/WALLET_RESPONSE)
2. ✅ Implement AgbaraWallet peer discovery via Nostr NIP-05
3. ✅ Wire wallet ViewModel to send requests via NostrClient
4. ✅ Handle responses in WalletViewModel state updates
5. ✅ Implement offline fallback: queue transactions when backend unavailable

**Key Files to Create:**
- `/app/src/main/java/com/ikoro/android/wallet/protocol/WalletProtocol.kt`
- `/app/src/main/java/com/ikoro/android/wallet/protocol/WalletRequestHandler.kt`
- `/app/src/main/java/com/ikoro/android/wallet/data/remote/WalletRemoteSource.kt`

### 2. NOSTR MESSAGING - ALREADY IMPLEMENTED ✅

**Current State:** Full Nostr client already exists (NostrClient.kt, 308 lines)

**What's Missing for Wallet:**
- wallet-specific message handlers (not just geohash chats)
- AgbaraWallet peer discovery endpoints
- Transaction signing verification over Nostr

**Implementation:**
1. Subscribe to wallet command messages from backend peers
2. Handle WALLET_BALANCE_REQUEST → response with encrypted balance
3. Handle WALLET_TX_SUBMIT_REQUEST → forward to mesh for signature
4. Handle WALLET_TX_STATUS_REQUEST → return transaction status

**Already Done:** NostrClient.subscribeToPrivateMessages() works!

### 3. WALLET ONBOARDING (Mnemonics, Seed, HD) - CRITICAL

**What's Missing:**
- BIP-39 mnemonic phrase generation
- BIP-32 HD path derivation
- Seed backup screen with BIP-39 wordlist
- Wallet restoration from backup phrase
- Multiple wallet support

**Serverless Implementation:**
```kotlin
// Libraries to add to build.gradle.kts:
// - android-bip39 (Mnemonic generation)
// - bitcoinj (HD derivation) - OR implement minimal derivation

implementation("com.github.bitcoinj:bitcoinj:0.16.2") // OR
implementation("com.github.komputing:BIP39:1.1.4")     // Lighter option
```

**Wallet Creation Flow:**
1. User enters "Create New Wallet"
2. Generate 12/24-word mnemonic (BIP-39)
3. Generate seed from mnemonic
4. Derive wallet keys from seed (BIP-32)
5. Show mnemonic with warnings (memorize, don't screenshot)
6. Ask user to "Confirm words in order" (backup verification)
7. Create wallet entries for BTC, OFO, NGN, USDT, USDC
8. Store encrypted keys in Android Keystore
9. Show QR for each currency address

**Files to Create:**
- `/app/src/main/java/com/ikoro/android/wallet/onboarding/MnemonicGenerator.kt`
- `/app/src/main/java/com/ikoro/android/wallet/onboarding/WalletCreationWizard.kt`
- `/app/src/main/java/com/ikoro/android/wallet/onboarding/SeedVault.kt`
- `/app/src/main/java/com/ikoro/android/wallet/onboarding/HDWalletDerivation.kt`

### 4. REAL QR SCANNING - SIMPLE ADDITION

**What's Missing:**
- QR scanning capability (only generating exists)
- CameraX integration
- ML Kit barcode scanning

**Implementation:**
1. Add CameraX dependencies (already in libs.versions.toml)
2. Create QRScannerFragment/Composable
3. Use ML Kit barcode scanner
4. Parse scanned QR (Bitcoin:bitcoinaddress?amount=0.01)
5. Route to Send screen with pre-filled data

**Files to Create:**
- `/app/src/main/java/com/ikoro/android/wallet/ui/scanner/QRScannerScreen.kt`
- `/app/src/main/java/com/ikoro/android/wallet/services/QRScannerService.kt`

### 5. PLAY STORE PREPARATION - ADMIN CONFIGURATION

**What's Missing:**
- Feature graphics (1024x500 pixels)
- Play Store listing content
- Privacy policy URL
- App content rating
- Release notes

**Implementation:**
1. Create feature graphics: Use Figma/Canva template
2. Create privacy policy.md (local file)
3. Upload to GitHub Pages or hosting
4. Complete Play Store console checklist

**Files to Create:**
- `/app/src/main/play/listings/en-US/full_description.txt`
- `/app/src/main/play/listings/en-US/short_description.txt`
- `/app/src/main/play/listings/en-US/feature_graphic.png`
- `/app/src/main/play/privacy-policy.html`

### 6. TRANSACTION SYNCHRONIZATION - USING MESH + QUEUE

**Current State:** TransactionQueueManager exists

**Missing:**
- Real mesh-based transaction broadcasting
- Transaction verification from peers
- Conflict resolution for double-spends

**Serverless Sync Strategy:**
```
Transaction Flow:
┌──────────────────────────────────────────────────────────────┐
│ 1. User creates transaction (Send screen)                    │
│ 2. Transaction signed with local private key                 │
│ 3. Queue added to TransactionQueueManager                    │
│ 4. Broadcast to mesh peers via MeshBroadcastService          │
│ 5. Peers verify and rebroadcast                              │
│ 6. Backend peer (AgbaraWallet) receives and processes        │
│ 7. Receipt broadcast back to sender                          │
│ 8. Transaction marked SUCCESS in local database              │
└──────────────────────────────────────────────────────────────┘

Offline Behavior:
- Transactions stay in PENDING queue
- When online, retries broadcasts
- No backend dependency for send (mesh handles it)
```

**Files to Complete:**
- `/app/src/main/java/com/ikoro/android/wallet/data/model/Transaction.kt` (already exists)
- `/app/src/main/java/com/ikoro/android/wallet/service/MeshTransactionBroadcaster.kt` (needs implementation)

### 7. ERROR HANDLING + CRASH REPORTING

**Current State:** Minimal exception handling

**What's Missing:**
- No crash reporting (Firebase Crashlytics)
- No user-friendly error messages
- No error recovery paths

**Serverless Crash Handling:**
Since we avoid external dependencies, implement local-only:
1. Global exception handler (Thread.UncaughtExceptionHandler)
2. Local error log file (app/private/logs/crash.log)
3. UI error indicators (snackbar with retry)
4. Retry queue for failed operations

**Implementation:**
```kotlin
// Add to WalletApplication.kt:
class WalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Global error handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrashLog(throwable)
            // Show error UI if in foreground
            // Exit gracefully
        }
    }
}
```

**File to Create:**
- `/app/src/main/java/com/ikoro/android/util/ErrorLogger.kt`

### 8. SETTINGS SCREENS - MEDIUM EFFORT

**What's Missing:**
- No settings screen with preferences
- No theme toggle
- No notification settings
- No relay configuration

**Implementation:**
- Use Jetpack Compose Settings lib
- Create SettingsFragment/Composable
- Store preferences in EncryptedSharedPreferences

**Files to Create:**
- `/app/src/main/java/com/ikoro/android/wallet/ui/settings/SettingsScreen.kt`
- `/app/src/main/java/com/ikoro/android/wallet/ui/settings/SettingsViewModel.kt`

### 9. TESTING INFRASTRUCTURE

**What's Missing:**
- No unit tests
- No UI tests
- No test seed wallets

**Implementation:**
Add instrumentation tests for critical flows:
- Wallet creation
- Transaction signing
- QR scanning
- Mesh broadcast

**File to Create:**
- `/app/src/androidTest/java/com/ikoro/android/wallet/WalletTests.kt`
- `/app/src/test/java/com/ikoro/android/wallet/WalletViewModelTest.kt`

## ARCHITECTURE PICTURE: Where Nostr Fits

**Current Stack:**
```
User App (ikorochat)
├── Nostr Client (NIP-01/05/04/17)
│   ├── RelayPool (public/private relays)
│   ├── Identity (npub Private key)
│   └── Messaging (gift wraps, geohash)
│
├── Mesh Layer (Bluetooth/Ethernet)
│   ├── PeerManager (tracked peers)
│   ├── SecurityManager (Noise protocol)
│   └── MessageHandler (broadcasts)
│
└── Wallet Module
    ├── WalletClient ← Connects via Nostr NIP-17 to AgbaraWallet
    ├── TransactionQueue (mesh broadcast)
    ├── Keystore (local signing)
    └── UI (Jetpack Compose)
```

**Nostr as Backend Replacement:**
```
Traditional (With Server):
App → HTTPS API → Backend Server → Database → External Services

Serverless (Our Approach):
App → Nostr NIP-17 → Backend Peer (AgbaraWallet) → Mesh Sync → Database
                              ↓
                       Also syncs via mesh to peer wallets
```

## DIFFERENTIATION: What Makes Us Unique

### Current Wallets:
- Trust Wallet: Centralized backend, requires server
- MetaMask: Browser extension, centralized
- Phantom: Solana-focused, centralized relays
- Sparrow: Desktop only, no mobile

### Ikoro Wallet Advantages:
1. **Peer-to-Peer Backend:** No central server needed
2. **Nostr Native:** First Nostr-integrated mobile wallet
3. **Mesh Network Support:** Works without internet (via Bluetooth)
4. **Offline-First:** Transactions queue locally
5. **Privacy-First:** Zero telemetry, local storage only
6. **Bitcoin + Stacks Ecosystem:** Bitcoin + OFO token
7. **Naira First:** Built-in NGN support
8. **Open Source:** Transparent, community verifiable

## PROPOSED MINIMUM VIABLE PRODUCT (MVP) TIMELINE

### Phase 1: Core Functionality (Weeks 1-2)
- [x] Wallet UI screens (DONE)
- [x] Security manager (DONE)
- [ ] Wallet creation/restore wizard (NEW)
- [ ] MNemonic generator + BIP-39 integration (NEW)
- [ ] HD wallet key derivation (NEW)
- [ ] QR code scanning (NEW)

### Phase 2: Backend Integration (Weeks 3-4)
- [ ] Nostr wallet protocol messages (NEW)
- [ ] AgbaraWallet peer discovery (NEW)
- [ ] Transaction signing and broadcasting (NEW)
- [ ] Mesh-based sync (NEW)

### Phase 3: User Experience (Weeks 5-6)
- [ ] Settings screens (NEW)
- [ ] Error handling and logging (NEW)
- [ ] Transaction history UI polish (NEW)
- [ ] Balance sync with mesh (NEW)

### Phase 4: Launch Prep (Weeks 7-8)
- [ ] Play Store listing (NEW)
- [ ] Testing (NEW)
- [ ] Documentation (NEW)
- [ ] Community announcement (NEW)

## CRITICAL FILES TO CREATE/MODIFY

### New Files Required:
1. `/app/src/main/java/com/ikoro/android/wallet/onboarding/MnemonicGenerator.kt`
2. `/app/src/main/java/com/ikoro/android/wallet/onboarding/WalletCreationWizard.kt`
3. `/app/src/main/java/com/ikoro/android/wallet/protocol/WalletProtocol.kt`
4. `/app/src/main/java/com/ikoro/android/wallet/protocol/WalletRequestHandler.kt`
5. `/app/src/main/java/com/ikoro/android/wallet/ui/scanner/QRScannerScreen.kt`
6. `/app/src/main/java/com/ikoro/android/wallet/ui/settings/SettingsScreen.kt`
7. `/app/src/main/java/com/ikoro/android/util/ErrorLogger.kt`

### Modify Existing Files:
1. `/app/src/main/java/com/ikoro/android/wallet/ui/viewmodel/WalletViewModel.kt`
   - Add Nostr client communication
   - Add state for backend connection
   - Add error handling

2. `/app/src/main/java/com/ikoro/android/wallet/data/repository/WalletRepository.kt`
   - Add remote source + local source

3. `/app/src/main/AndroidManifest.xml`
   - Add camera permissions for QR scanning
   - Add settings activity/fragment

4. `/app/build.gradle.kts`
   - Add BIP-39 library
   - Add testing dependencies

## TECHNICAL DEPENDENCIES TO ADD

### For Wallet Onboarding:
```kotlin
// BIP-39 mnemonic generation
implementation("com.github.komputing:BIP39:1.1.4") // OR

// HD wallet derivation (minimal implementation available)
// OR use bitcoinj for full compatibility
```

### For QR Scanning (Already in versions.toml):
```kotlin
// CameraX already present in dependencies
// ML Kit barcode scanning already present
```

### For Testing (Add):
```kotlin
// Unit testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:4.1.0")
testImplementation("androidx.arch.core:core-testing:2.2.0")

// Instrumentation testing
androidTestImplementation("androidx.test.ext:junit:1.2.1")
androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
```

## CONCLUSION: Serverless is Possible!

The app already has Nostr and mesh networking infrastructure. We can:

1. Use Nostr NIP-17 as encrypted private messaging to AgbaraWallet
2. Use mesh for peer-to-peer transaction broadcasting
3. Keep wallet operations fully local (signing, keys)
4. Add minimal backend via peer-to-peer (not centralized server)
5. Maintain privacy-first approach (no telemetry)

**No external server required.**
**No third-party API key required.**
**No database hosting required.**
**All sync happens via Nostr + mesh network.**

═══════════════════════════════════════════════════════════════
Prepared by: Hermes Agent
Date: 2026-06-04
Status: Plan ready for permission before implementation
═══════════════════════════════════════════════════════════════
