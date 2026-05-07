# Ikoro Android - ₿ỌFỌ Platform

## Overview

Ikoro is a privacy-focused, offline-first messaging and e-commerce platform built for emerging markets. Powered by mesh networking technology, it enables communication, commerce, and emergency response without relying on internet connectivity.

### Key Features

- **Peer-to-Peer Chat** - Encrypted messaging with mesh network support
- **Marketplace** - Full e-commerce platform (10+ categories) with ₿ transactions
- **Multimedia** - Image/video sharing, voice messages, media streaming
- **Wallet** - ₿ and ₿ỌFỌ wallet with offline transaction queueing
- **Orders** - Order tracking and management (100% offline view)
- **AI Assistant (Agbara)** - Chat intelligence, recommendations, spending insights
- **Emergency Mode** - SOS broadcasts, location sharing, offline first aid guides
- **Creator Economy** - Music DAW, DJ mixer, VFX studio, AI creative tools
- **Errand Logistics** - Request runners, track deliveries, ratings
- **Security** - PIN/biometric auth, fraud detection, encrypted storage

### Tech Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Database**: Room (SQLite)
- **Networking**: OkHttp, Retrofit (when internet available)
- **Image Loading**: Coil
- **Dependency Injection**: Hilt
- **Coroutines**: kotlinx.coroutines
- **Async Stream**: Kotlin Flow
- **Architecture**: MVVM + Clean Architecture

### Architecture

```
┌─────────────────────────────────────────────┐
│          Presentation Layer                 │
│        (Jetpack Compose UI)                │
│   Screens, ViewModels, Composeables       │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│           Domain Layer                      │
│   (Business Logic, Use Cases, Models)     │
│   Security, Encryption, Fraud Detection    │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│          Data Layer                         │
│   Room Database, Mesh Sync, Cache         │
│   Local Storage (Encrypted)               │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         Network Layer                       │
│   Mesh Network (无互联网Media)              │
│   OkHttp (Internet Optional)              │
└─────────────────────────────────────────────┘
```

---

## Project Structure

```
app/src/main/java/com/ikoro/android/
├── ai/                          # AI Assistant (Agbara)
│   ├── AgbaraAssistant.kt      # AI capability definitions
│   └── services/               # AI services (chat intelligence, etc.)
├── auth/                       # Authentication
│   ├── BiometricAuth.kt        # Biometric authentication
│   ├── PINManager.kt          # PIN management
│   └── SecurityValidator.kt   # Security validation and fraud detection
├── chat/                       # Chat functionality
│   ├── ChatManager.kt        # Chat message handling
│   ├── MeshMessaging.kt      # Mesh network messaging
│   └── MultimediaHandler.kt  # Image/video/audio handling
├── creator/                    # Creator economy
│   ├── data/model/           # Music projects, DJ mixes, VFX models
│   └── ui/screens/           # Creator Studio UI
├── ecommerce/                 # E-commerce platform
│   ├── data/model/           # Product, Order, Transaction models
│   ├── data/model/ProductModels.kt  # Extended product (images, videos, seller)
│   ├── data/model/CategoryModels.kt  # Specialized categories (healthcare, education, etc.)
│   ├── ui/screens/            # Marketplace, Product Detail, Live Shopping
│   └── ui/screens/categories/ # Category-specific screens
│       ├── HealthcareScreen.kt
│       ├── EducationScreen.kt
│       ├── CategoryScreens.kt (Agriculture, RealEstate, Automotive, Fashion, Food, Services)
├── emergency/                 # Emergency mode
│   └── data/model/           # Emergency broadcasts, first aid guides
├── mesh/                      # Mesh networking
│   ├── MeshNode.kt           # Mesh peer discovery and management
│   ├── MeshSync.kt           # Mesh synchronization
│   └── MeshSecurity.kt       # Mesh encryption and reputation
├── security/                  # Security and privacy
│   └── data/model/           # Security settings, fraud detection, biometrics
├── ui/                        # UI components
│   ├── EcommerceNavigation.kt # Bottom navigation (7 tabs)
│   └── screens/              # Shared UI screens
│       ├── AgendaScreen.kt   # (Placeholder)
│       ├── MarketplaceScreen.kt
│       ├── MediaScreen.kt    # (Placeholder)
│       ├── AgbaraAssistantScreen.kt
│       ├── WalletScreen.kt
│       ├── OrdersScreen.kt
│       └── ErrandScreen.kt
```

---

## Development

### Prerequisites

- Android Studio Hedgehog (2023.1.1+) or IntelliJ IDEA
- JDK 17
- Android SDK API 24+ (Android 7.0+)
- Gradle 8.0+

### Building

```bash
# Clone repository
git clone <repository-url>
cd ikorochat-android

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run Espresso tests
./gradlew connectedDebugAndroidTest
```

### Releases

Release APKs include:
- Code obfuscation (R8/ProGuard)
- Optimized for size and startup
- Removed debug features
- Network security configuration

To sign release APK:

```bash
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=$KEYSTORE_FILE \
  -Pandroid.injected.signing.store.password=$KEYSTORE_PASSWORD \
  -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
  -Pandroid.injected.signing.key.password=$KEY_PASSWORD
```

---

## User Guide

### First Launch

1. **Create Profile**: Enter name, optionally phone/email
2. **Set PIN**: Choose 4-6 digit PIN (required for transactions)
3. **Enable Biometric**: Optional (fingerprint/face)
4. **Sync Mesh**: Auto-discovers nearby mesh nodes
5. **Initialize Wallet**: Default 0 ₿ (can add via QR/transfer)

### Key Screens

- **Chat Tab (`Chat` icon)**: Peer-to-peer encrypted messaging
- **Marketplace Tab (`Storefront` icon)**: Browse and buy products (10 categories)
- **Media Tab (`PhotoLibrary` icon)**: Share images, videos, audio
- **AI Tab (`Psychology` icon)**: Agbara AI assistant (recommendations, insights)
- **Wallet Tab (`AccountBalanceWallet` icon)**: ₿/₿ỌFỌ balance + transaction history
- **Orders Tab (`ShoppingBasket` icon)**: Active and completed orders
- **Errand Tab (`DeliveryDining` icon)**: Request errands + track deliveries

### Making a Purchase

1. Go to Marketplace tab
2. Select category (Healthcare, Education, Agriculture, etc.)
3. View product details (images, videos, reviews, seller info)
4. Tap "Buy Now" or "Add to Cart"
5. Review order in checkout
6. Confirm payment: Enter PIN/Biometric approval
7. Order appears in Orders tab
8. Track status (Pending → Processing → Delivered)

### Payment Methods

- **QR Code Scan**: Scan seller's QR code
- **Payment Link': Click link from Chat/Email
- **Near Field Communication (NFC)**: Tap to pay
- **Wallet Balance**: Direct from ₿/₿ỌFỌ wallet
- **Escrow**: For marketplace purchases, funds held until delivery confirmed

### Emergency Mode

**Quick Access**:
- Shake phone (SOS shortcut setting optional)
- Tap Emergency tab or home screen shortcut
- Tap Red SOS button

**Options**:
- **Emergency Type**: Medical, Accident, Fire, Flood, Crime, etc.
- **Location Sharing**: Auto-send via mesh network
- **First Aid Guides**: Access offline guides (CPR, Bleeding, Burns, etc.)
- **Help Response**: Responders nearby will see your broadcast and can respond

### Privacy & Security

- **PIN**: Required for transactions > 0.01 ₿
- **Biometric**: Fingerprint/face available (backed by device)
- **Mesh Encryption**: All messages encrypted end-to-end
- **Fraud Detection**: Alerts for unusual patterns (new device, large transaction, unusual location)
- **Two-Factor**: Optional SMS/Email/Offline code
- **Anonymity Mode**: Hide online status and profile visibility
- **Offline Wallet**: Private keys stored locally (never leave device)

---

## API Reference

### Core Models

#### Product

```kotlin
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,                  // in ₿
    val sellerId: String,
    val sellerName: String,
    val images: List<String>,
    val inStock: Boolean = true,
    val createdAt: Long
)
```

#### Order

```kotlin
data class Order(
    val id: String,
    val productId: String,
    val buyerId: String,
    val sellerId: String,
    val quantity: Int,
    val totalAmount: Double,            // in ₿
    val status: OrderStatus,
    val createdAt: Long,
    val estimatedDelivery: Long? = null
)
```

#### Transaction

```kotlin
data class Transaction(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,                 // in ₿
    val currency: Currency,
    val type: TransactionType,
    val status: TransactionStatus,
    val timestamp: Long,
    val location: String? = null,
    val metadata: String? = null
)
```

#### Wallet

```kotlin
data class Wallet(
    val userId: String,
    val balances: Map<Currency, Double>,  // ₿, ₦, ₿ỌFỌ
    val transactions: List<Transaction> = emptyList(),
    val lastSync: Long
)
```

### Domain Services

#### ChatManager

```kotlin
class ChatManager {
    suspend fun sendMessage(peerId: String, message: String, attachments: List<AttachmentMessage>? = null)
    suspend fun receiveMessages(since: Long): List<ChatMessage>
    suspend fun syncWithMesh(nodeId: String)
}

class MeshMessaging {
    fun broadcastMessage(message: MeshMessage, proximity: Proximity)
    fun receiveFromNearby(): Flow<IncomingMeshMessage>
    fun signMessage(message: String): SignedMessage
    fun verifySignature(message: SignedMessage): Boolean
}
```

#### WalletManager

```kotlin
class WalletManager {
    suspend fun addFunds(amount: Double, currency: Currency)
    suspend fun transfer(toUserId: String, amount: Double, currency: Currency)
    suspend fun checkBalance(currency: Currency): Double
    suspend fun getTransactionHistory(limit: Int = 50): List<Transaction>
    suspend fun createTransaction(toUserId: String, amount: Double, currency: Currency, pin: String): TransactionResult
}
```

#### OrderManager

```kotlin
class OrderManager {
    suspend fun createOrder(productId: String, quantity: Int): Order
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
    suspend fun getOrders(buyerId: String): List<Order>
    suspend fun syncOrderStatus(orderId: String)
}
```

#### SecurityValidator

```kotlin
class SecurityValidator {
    fun validatePIN(enteredPIN: String): PINValidation
    fun validateBiometric(): Boolean
    fun detectFraud(transaction: Transaction): FraudDetectionResult
    fun checkSecurityContext(deviceId: String): SecurityContext
}
```

#### EmergencyManager

```kotlin
class EmergencyManager {
    suspend fun broadcastEmergency(type: EmergencyType, location: Location, message: String)
    suspend fun getEmergenciesNearby(): Flow<EmergencyBroadcast>
    fun getFirstAidGuide(category: FirstAidCategory): FirstAidGuide
    fun checkFirstAidGuidesAvailable(): Boolean
}
```

---

## Configuration

### App Configuration

Located in `app/src/main/res/values/strings.xml`
- App name, currency symbols, network URLs

### Network Configuration

Located in `app/src/main/res/xml/network_security_config.xml`
- Pinning for server connections (when internet available)
- Mesh port configuration

### Security Configuration

Located in `app/src/main/java/com/ikoro/android/security/data/model/SecurityModels.kt`
- Default PIN length (4-6 digits)
- Transaction limits (0.5 ₿)
- Lockout timeout (5 minutes after 5 failed attempts)

---

## Performance Targets

- **App cold start**: < 2s
- **Marketplace load**: < 2s (100 products)
- **Image load**: < 500ms (compressed)
- **Wallet transaction**: < 500ms (pending)
- **Chat message send**: < 300ms
- **Memory idle**: < 150MB
- **Memory max**: < 350MB
- **Battery drain (active)**: < 5%/hr
- **Battery drain (idle)**: < 1%/hr

详见 PERFORMANCE.md for full optimization checklist。

---

## Testing

参见 TESTING.md for comprehensive testing guidelines:
- Unit tests (JUnit 5, MockK)
- Integration tests (androidx.test)
- UI tests (Espresso)
- Performance benchmarks
- Security tests
- Offline/mesh tests

Run all tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## Contributing

### Code Style

- Kotlin naming conventions (camelCase, PascalCase)
- Jetpack Compose best practices
- 2-space indentation (following Kotlin convention)
- Comments for public APIs
- KDoc for classes (> 10 lines)

### Git Conventional Commits

```
type(scope): description

Types:
- feat: New feature
- fix: Bug fix
- refactor: Code refactoring (no behavior change)
- docs: Documentation
- perf: Performance improvement
- test: Adding tests
- chore: Build/release tasks
- style: Formatting (no logic change)

Examples:
feat(marketplace): Add healthcare category support
fix(wallet): Transaction status update race condition
perf(mesh): Reduce sync operator batch size
docs(update): Add missing API reference
```

### Pull Request Requirements

- [ ] Unit tests added/updated
- [ ] Document changes in CHANGELOG.md
- [ ] Screenshots for UI changes
- [ ] Manual testing checklist marked
- [ ] No breaking changes (or migration plan)

---

## Troubleshooting

### App crashes after update
- Wipe app data
- Clear cache: `adb shell pm clear com.ikoro.android`
- Report to support with logs

### Mesh network not discovering peers
- Verify Bluetooth/Wi-Fi Direct permissions
- Check location permission (required for Bluetooth scanning)
- Try toggling network off/on
- Report with `adb logcat` output

### Wallet balance incorrect
- Sync with other wallets (tap "Refresh" in wallet)
- Check for stuck pending transactions
- Verify no sync errors in logs

### Emergency SOS not sending
- Verify ` manifestation Settings -> {Emergency SOS} -> Shake detection
- Check location permission
- Verify mesh nodes nearby
- Try manual emergency tab broadcast

---

## License

See LICENSE file.

---

## Support & Contributions

- Discord: https://discord.com/invite/clawd
- GitHub Issues: https://github.com/<repo>/issues
- Email: support@ikoro.ng

---

## Roadmap

### Next Releases

**[v1.1]**
- Live shopping audio/video streaming
- Enhanced creator tools (AI music generation)
- Multi-currency escrow
- Collectible digital assets marketplace

**[v1.2]**
- Marketplace analytics for sellers
- Enhanced AI assistant (voice)
- Community forum/marketplace
- Secure decentralized escrow integration

**[v2.0]**
- Cross-platform (iOS, Web)
- Beta marketplace API for third-party integrations
- Advanced anomaly detection for fraud
- Mesh-of-meshes (relay assistants)

---

## Credits

- Core Platform: Ikoro Team
- Mesh Network: TimeScape Foundation
- Design: Creative Commons Nigeria
- Contributors: Open Source Community

---

**Version: 1.0.0-alpha**  
**Last Updated: May 2026**  
**Build: 2cb8422+ (commits after base)**