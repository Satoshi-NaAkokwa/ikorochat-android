//
// SecurityModels.kt
// Ikoro - ₿ỌFỌ Platform
//
// Security & Privacy: Biometrics, PIN, fraud detection
//

package com.ikoro.android.security.data.model

/**
 * Security Settings
 */
data class SecuritySettings(
    val biometricEnabled: Boolean = false,
    val pinEnabled: Boolean = false,
    val pin: String? = null, // Encrypted
    val autoLockTimeout: Int = 5, // minutes
    val twoFactorEnabled: Boolean = false,
    val newDeviceNotifications: Boolean = true,
    val transactionLimit: Double = 0.5, // ₿ỌFỌ
    val lastSecurityCheck: Long = 0,
    val failedAttempts: Int = 0,
    val lockedUntil: Long = 0
)

/**
 * PIN Validation
 */
data class PINValidation(
    val isValid: Boolean,
    val isLocked: Boolean,
    val remainingAttempts: Int = 5,
    val lockoutEnds: Long = 0
)

/**
 * Biometric Method
 */
enum class BiometricMethod {
    FINGERPRINT,
    FACE,
    IRIS,
    VOICE
}

/**
 * Security Event
 */
data class SecurityEvent(
    val id: String,
    val type: SecurityEventType,
    val timestamp: Long,
    val deviceId: String,
    val location: String? = null,
    val details: String,
    val isSuspicious: Boolean = false
)

enum class SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    PIN_ADMISSION,
    BIOMETRIC_ADMISSION,
    TRANSACTION,
    SETTINGS_CHANGE,
    PASSWORD_CHANGE,
    NEW_DEVICE_LOGIN,
    LOCATION_CHANGE,
    UNUSUAL_PATTERN
}

/**
 * Fraud Detection Result
 */
data class FraudDetectionResult(
    val isSuspicious: Boolean,
    val riskLevel: RiskLevel,
    val flags: List<FraudFlag>,
    val recommendations: List<String>,
    val timestamp: Long
)

enum class RiskLevel {
    SAFE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class FraudFlag {
    UNUSUAL_LOCATION,
    NEW_DEVICE,
    LARGE_TRANSACTION,
    RAPID_TRANSACTIONS,
    OFF_PATTERN_OCCURRED,
    ASYNCHRONOUS_TRANSACTION,
    BEEN_COMPROMISED_DEVICE,
    SUSPICIOUS_BEHAVIOR,
    DEVICE_TAMPERING,
    NETWORK_ANOMALY
}

/**
 * Transaction Security Check
 */
data class TransactionSecurityCheck(
    val transactionId: String,
    val amount: Double,
    val recipient: String,
    val requesterDevice: String,
    val requesterLocation: String,
    val isApproved: Boolean,
    val blockedReason: String? = null,
    val verificationRequired: Boolean = false,
    val riskLevel: RiskLevel
)

/**
 * Device Information
 */
data class DeviceInfo(
    val id: String,
    val name: String,
    val type: DeviceType,
    val firstSeen: Long,
    val lastSeen: Long,
    val isActive: Boolean = false,
    val isTrusted: Boolean = true,
    val locationHistory: List<LocationAccess> = emptyList(),
    val platform: String,
    val osVersion: String
)

enum class DeviceType {
    PHONE,
    TABLET,
    DESKTOP,
    WEB,
    WATCH
}

data class LocationAccess(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val city: String? = null
)

/**
 * Privacy Settings
 */
data class PrivacySettings(
    val profileVisible: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val allowMeshDiscovery: Boolean = true,
    val shareLocationServices: Boolean = false,
    val dataEncryption: Boolean = true,
    val anonymityMode: Boolean = false,
    val transactionPrivacy: TransactionPrivacy = TransactionPrivacy.PUBLIC
)

enum class TransactionPrivacy {
    PUBLIC,
    FRIENDS_ONLY,
    PRIVATE
}

/**
 * Offline Security Features
 */
data class OfflineSecurityFeatures(
    val encryptedStorage: Boolean = true,
    val localAuthentication: Boolean = true,
    val meshEncryption: Boolean = true,
    val broadcastEncryption: Boolean = true,
    val QRSecurity: Boolean = true,
    val nfcSecurity: Boolean = true
)

/**
 * Two-Factor Authentication Method
 */
enum class TwoFactorMethod {
    SMS,
    EMAIL,
    AUTHENTICATOR_APP,
    BIOMETRIC,
    OFFLINE_CODE
}

/**
 * Security Alert
 */
data class SecurityAlert(
    val id: String,
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val actionRequired: Boolean = false,
    val actionLabel: String? = null,
    val timestamp: Long,
    val dismissed: Boolean = false
)

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

/**
 * Wallet Backup
 */
data class WalletBackup(
    val id: String,
    val encryptedPhrase: String, // Encrypted seed phrase
    val backupDate: Long,
    val deviceInfo: String,
    val storageLocation: String, // e.g., "Google Drive", "iCloud", "Local file"
    val lastVerified: Long,
    val isVerified: Boolean = false
)

/**
 * Mesh Security Layer
 */
data class MeshSecurityLayer(
    val nodeId: String,
    val encryptionKey: String,
    val signedMessages: Boolean = true,
    val reputationScore: Float = 1.0f,
    val blockList: List<String> = emptyList(),
    val trustedNodes: List<String> = emptyList()
)

/**
 * Payment Security
 */
data class PaymentSecurity(
    val useDefaultPaymentMethod: Boolean = false,
    val requireConfirmation: Boolean = true,
    val limitBeforeConfirmation: Double = 0.1,
    val allowQuickPay: Boolean = true,
    val quickPayLimit: Double = 0.005,
    val merchantVerification: Boolean = true,
    val qrCodeExpiration: Int = 300, // seconds
    val paymentLinkExpiration: Int = 86400 // seconds (24h)
)

/**
 * Identity Data Storage (Encrypted)
 */
data class IdentityData(
    val userId: String,
    val encryptedName: String,
    val encryptedPhone: String? = null,
    val encryptedEmail: String? = null,
    val encryptedAddress: String? = null,
    val encryptedBiometricData: String? = null,
    createdAt: Long,
    lastUpdated: Long
)

/**
 * Security Audit Log
 */
data class SecurityAuditLog(
    val id: String,
    action: String,
    performedBy: String,
    timestamp: Long,
    success: Boolean,
    details: String? = null,
    deviceInfo: String
)

/**
 * Account Recovery Settings
 */
data class AccountRecovery(
    val contactMethod: RecoveryMethod,
    val verificationData: String, // Encrypted phone/email
    val backupQuestions: List<String> = emptyList(),
    val lastVerified: Long,
    val recoveryCodeExpiration: Int = 3600 // seconds
)

enum class RecoveryMethod {
    MESH_MESSAGE,
    TRUSTED_CONTACT,
    BACKUP_CODE,
    QR_RECOVERY
}

/**
 * PIN Entry State
 */
data class PINEntryState(
    val enteredDigits: String = "",
    val showSuccess: Boolean = false,
    val showFailure: Boolean = false,
    val attemptCount: Int = 0,
    val shakeTriggered: Boolean = false
)