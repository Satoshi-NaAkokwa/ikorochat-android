# Testing Guidelines - Phase 14
# Ikoro Android - ₿ỌFỌ Platform

## Unit Testing

### Test Coverage Requirements
- [ ] Core business logic (Wallet, Orders): 80%+
- [ ] Security models (PIN, Fraud detection): 90%+
- [ ] Product data models: 70%+
- [ ] Message handling: 75%+
- [ ] Sync logic: 65%+

### Testing Frameworks
- [x] JUnit 5 for unit tests (test directory)
- [ ] androidx.test for integration tests (androidTest directory)
- [ ] MockK for Kotlin mocking
- [ ] Turbine for Flow testing
- [ ] Robolectric for UI-less Android tests

### Critical Unit Tests
```kotlin
data class WalletSecurityChecks
    - PIN validation
    - Fraud detection (suspicious patterns)
    - Transaction limits enforcement
    - Biometric fallback logic

data class ProductManagement
    - Price calculation
    - Stock validation
    - Category assignment
    - Currency formatting (₿ display)

data class MessageSecurity
    - Encryption/decryption
    - Mesh network signing
    - Reply message protection

data class EmergencyFunctionality
    - First aid guide retrieval
    - Emergency broadcast creation
    - Location sharing
```

---

## Integration Testing

### Test Scenarios
- [ ] Wallet transaction flow (create -> sync -> verify)
- [ ] Order placement flow (product -> cart -> checkout -> payment)
- [ ] Peer-to-peer chat with encryption
- [ ] Marketplace product search and filter
- [ ] Category-specific screens (Healthcare, Education, etc.)
- [ ] Emergency broadcast reception in mesh mode
- [ ] Wallet balance synchronization across devices
- [ ] Creator studio project saving/loading
- [ ] Errand request and tracking

### Fragment Testing
- [ ] MarketplaceScreen - product list loading
- [ ] WalletScreen - balance display, transaction history
- [ ] OrdersScreen - order status updates
- [ ] ChatScreen - message send/receive
- [ ] EmergencyScreen - SOS broadcast
- [ ] CreatorStudioScreen - project tabs navigation
- [ ] CategoryScreens - Healthcare, Education, etc.

---

## UI Testing (Espresso)

### Critical UI Flows
- [ ] Send message with text
- [ ] Send message with image
- [ ] Buy product from marketplace
- [ ] Add to cart and checkout
- [ ] Create wallet transaction
- [ ] View transaction history
- [ ] Scan QR code
- [ ] Generate payment QR code
- [ ] Emergency SOS tap successfully
- [ ] PIN entry and validation
- [ ] Biometric entry (if supported)
- [ ] Navigation between tabs (Chat, Marketplace, Media, AI, Wallet, Orders, Errand)
- [ ] Search products in marketplace
- [ ] View product details with images/videos
- [ ] Filter products by category
- [ ] Create music project in studio
- [ ] Create DJ mix
- [ ] Request errand
- [ ] Track errand status
- [ ] View first aid guide offline

### Accessibility Testing
- [ ] TalkBack support verified
- [ ] Font scaling up to 200%
- [ ] High contrast mode
- [ ] Touch target > 48dp
- [ ] Content descriptions on all interactive elements
- [ ] Heading hierarchy correct
- [ ] Focus order logical

---

## Performance Testing

### Load Testing Scenarios
- [ ] 500 products in marketplace (scroll smooth)
- [ ] 1000 messages in chat (pagination working)
- [ ] 50 active orders (load < 2s)
- [ ] 20 wallet transactions (balance updates instant)
- [ ] 10 emergency broadcasts nearby (priority handling)
- [ ] Mesh sync with 100 nodes (bandwidth efficient)

### Memory Leak Testing
- [ ] No leaks when rotating screen repeatedly
- [ ] No leaks when opening/closing Drawer
- [ ] No leaks when deep linking to product details
- [ ] No leaks after 10.opensdeep navigation
- [ ] Cache cleared on low memory

### Battery Testing
- [ ] < 5% used per hour (active)
- [ ] < 1% used per hour (idle)
- [ ] Sync respects battery saver mode
- [ ] Background sync minimal when not charging

---

## Security Testing

### PIN/Authentication Testing
- [ ] PIN must be 4-6 digits
- [ ] Too many attempts trigger lockout (5 attempts = 5 min)
- [ ] Wrong PIN shows error after validation
- [ ] Correct PIN shows success immediately
- [ ] PIN hidden (masked) with fallback visibility
- [ ] Biometric fallback available when enabled
- [ ] Biometric rejected triggers PIN fallback
- [ ] Reset PIN requires proper validation

### Encryption Testing
- [ ] Messages decrypted by recipient only
- [ ] Messages cannot be read by other peers
- [ ] Emergency broadcasts signed by sender
- [ ] Wallet transactions signed by sender
- [ ] QR codes cannot be forged
- [ ] Offline-only data stored encrypted

### Fraud/Pattern Testing
- [ ] Unusual location flagged
- [ ] New device transaction flagged
- [ ] Large transaction (above limit) flagged
- [ ] Rapid consecutive transactions flagged
- [ ] Multiple PIN failures flagged
- [ ] Suspicious network activity flagged

### Mesh Network Security Testing
- [ ] Malicious node messages rejected
- [ ] Spam broadcasts filtered
- [ ] Blacklisted nodes blocked
- [ ] Trust/repurposing score updated
- [ ] Replay attacks prevented (timestamps checked)

---

## End-to-End (E2E) Testing

### Critical User Journeys
```kotlin
1. Marketplace Purchase Flow:
   - Open Marketplace
   - Search for product
   - View product details
   - Add to cart/checkout
   - Pay with ₿
   - Order appears in Orders tab
   - Status updates in real-time

2. Peer-to-Peer Chat:
   - Open chat with peer
   - Send text message
   - Receive response (encrypted)
   - Send image
   - Verify encrypted on mesh

3. Emergency Response Flow:
   - Open Emergency tab (or shortcut)
   - Tap SOS button
   - Select emergency type
   - Emergency broadcast sent via mesh
   - Nearby nodes receive alert
   - Responders show up in list
   - Responders respond to broadcast

4. Wallet Management Flow:
   - View wallet balance
   - Add ₿ from another source
   - Send ₿ to peer
   - View transaction history
   - Check fraud/flags
   - PIN verification for > 0.01 ₿ transactions

5. Creator Economy Flow:
   - Open Creator Studio
   - Create music project
   - Add audio tracks
   - Save project
   - Publish (optional)
   - See in portfolio

6. Errand Request Flow:
   - Open Errand tab
   - Request errand (pickup, dropoff, items)
   - See estimated price
   - Submit request
   - Track runner location
   - Mark complete when delivered
```

---

## Offline Testing

### Offline Scenarios
- [ ] Marketplace loads cached products offline
- [ ] Chat messages sent/disconnected queue and sync when online
- [ ] Wallet transactions queued offline
- [ ] Emergency broadcasts sent via mesh only
- [ ] First aid guides accessible fully offline
- [ ] Product images cached for offline viewing
- [ ] Orders viewable offline (last 50)
- [ ] QR codes generate offline
- [ ] PIN/biometric work offline

### Deep Offline Testing
- [ ] Use only mesh network (no internet) for 24h
- [ ] No new syncs, only mesh-collected broadcasts
- [ ] Add/peer end devices via mesh
- [ ] Create orders offline - sync when back online
- [ ] Chat between meshed peers offline

---

## Compatibility Testing

### Device Matrix
- [ ] Low-end (4GB RAM, Android 7)
- [ ] Mid-end (6GB RAM, Android 11)
- [ ] High-end (8GB+ RAM, Android 13+)
- [ ] Tablets (8"+, different aspect ratio)
- [ ] Foldable/unfoldable devices
- [ ] Automotive head units (if applicable)

### Screen Density Testing
- [ ] MDPI (160 dpi)
- [ ] HDPI (240 dpi)
- [ ] XHDPI (320 dpi)
- [ ] XXHDPI (480 dpi)
- [ ] XXXHDPI (640 dpi)

---

## Testing Tools & Configuration
- [ ] Unit tests: `./gradlew test`
- [ ] Integration tests: `./gradlew connectedAndroidTest`
- [ ] Instrumented tests: `./gradlew connectedDebugAndroidTest`
- [ ] Espresso tests: `./gradlew connectedAndroidTest --tests com.ikoro.android.ui.*`
- [ ] Coverage report: `./gradlew jacocoTestReport`
- [ ] Parameter tests: `./gradlew test --parameter=ui`

To execute all tests for release APK:
```bash
# Run all tests
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest

# Run Espresso tests
./gradlew connectedAndroidTest --tests com.ikoro.android.ui.screens tests

# Generate test report
./gradlew jacocoTestReport
```

---

## Automated Testing (CI/CD)
- [ ] GitHub Actions/GitLab CI for unit tests on PR
- [ ] Espresso tests run daily on emulator
- [ ] Performance benchmarks weekly
- [ ] APK builds and smoke test on merge
- [ ] Beta build sent to test group weekly

---

## Known Issues & Workarounds
- [ ] List placeholder: [Priority] [Description] [Workaround]
- [ ] Navbar occasionally shrinks on low RAM: Use `androidx ConstraintLayout2.01`
<style>workaround</style>
- [ ] Espresso test fails on emoji rendering: Use `@VisibleForTesting` instead
- [ ] First aid guides formatting: Use proper markdown desde
- [ ] Emergency SMS fallback: Add fallback for devices without mesh

---

## Testing Signoff Checklist
Before releasing 1.0:
- [ ] 100% of unit tests passing (# tests >= 200)
- [ ] 90% of critical integration tests passing
- [ ] All UI flows tested and working
- [ ] Accessibility audited (WCAG AA)
- [ ] No known security vulnerabilities (owasp top 10)
- [ ] Performance targets met (APK < 30MB, loading < 2s)
- [ ] Offline mode verified (no crashes)
- [ ] Mesh mode verified (broadcasts work)
- [ ] Emergency mode verified (SOS triggers)
- [ ] Pixel perfect test (reference screenshots match)
- [ ] Crash-free rate > 99.5% (beta)
- [ ] 10 beta testers report no critical issues

---

## Test Case Examples

### Unit Test: PIN Validation
```kotlin
@Test
fun pinValidation_exceedsAttempts_returnsLocked() = runTest {
    val securityManager = SecurityManager()
    for (i in 1..6) {
        securityManager.validatePin("0000")
    }
    val result = securityManager.validatePin("0000")
    assert(result.isLocked)
    assert(result.remainingAttempts == 0)
}
```

### Espresso Test: Marketplace Purchase
```kotlin
@Test
fun marketplace_purchase_flow_succeeds() {
    // Given
    LoginActivity.enterPIN("1234")
    
    // When
    MarketplaceScreen.tapProduct("Wireless Headphones")
    ProductDetailScreen.tapBuyButton()
    CheckoutScreen.confirmPayment("₿0.05")
    
    // Then
    OrdersScreen.assertOrderPresent("Wireless Headphones")
    WalletScreen.assertBalanceReducedBy("₿0.05")
}
```

### UI Test: Emergency SOS
```kotlin
@Test
fun emergency_sos_broadcast_creates() {
    EmergencyScreen.tapSOS()
    EmergencySelectDialog.selectType("Medical")
    EmergencyConfirmDialog.confirm()
    
    MeshBroadcastList.assertMessagePresent(
        type="EMERGENCY",
        reporter="You",
        location="[current location]"
    )
}
```