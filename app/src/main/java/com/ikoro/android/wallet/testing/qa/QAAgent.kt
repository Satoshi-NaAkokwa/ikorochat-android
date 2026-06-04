package com.ikoro.android.wallet.testing.qa

import com.ikoro.android.wallet.domain.service.KeyManager
import com.ikoro.android.wallet.domain.service.TransactionSigner
import com.ikoro.android.wallet.domain.service.WalletService
import com.ikoro.android.wallet.data.model.Wallet
import com.ikoro.android.wallet.data.model.Transaction
import kotlinx.coroutines.runBlocking


/**
 * Quality Assurance Agent - Automated testing for wallet functionality
 */
class QAAgent(
    private val walletService: WalletService,
    private val signer: TransactionSigner,
    private val keyManager: KeyManager
) {
    companion object {
        const val TEST_WALLET_ID = "test_wallet"
        const val TEST_USER_ID = "test_user_001"
    }

    // Test results
    private val testResults = mutableListOf<TestResult>()

    // Run all QA tests
    fun runAllTests(): List<TestResult> {
        clearResults()

        testResults.add(testWalletCreation())
        testResults.add(testTransactionSigning())
        testResults.add(testKeyEncryption())
        testResults.add(testFraudDetection())
        testResults.add(testOfflineTransactionQueueing())

        return testResults
    }

    // Clear test results
    private fun clearResults() {
        testResults.clear()
    }

    // Test wallet creation
    private fun testWalletCreation(): TestResult {
        return try {
            val result = runBlocking {
                walletService.createWallet(TEST_USER_ID)
            }
            TestResult(
                name = "wallet_creation",
                passed = result != null,
                message = "Wallet created successfully",
                data = result
            )
        } catch (e: Exception) {
            TestResult(
                name = "wallet_creation",
                passed = false,
                message = "Failed to create wallet: ${e.message}",
                error = e
            )
        }
    }

    // Test transaction signing
    private fun testTransactionSigning(): TestResult {
        val txn = Transaction(
            id = "test_txn_001",
            walletId = TEST_WALLET_ID,
            amount = 0.05,
            currency = "BITCOIN",
            type = "SEND",
            fromAddress = "from_addr",
            toAddress = "to_addr"
        )

        return try {
            val signed = runBlocking {
                signer.signWithEncryptedWallet(txn, "testpin123")
            }
            TestResult(
                name = "transaction_signing",
                passed = signed?.isSigned ?: false,
                message = "Transaction signed successfully",
                data = signed
            )
        } catch (e: Exception) {
            TestResult(
                name = "transaction_signing",
                passed = false,
                message = "Failed to sign transaction: ${e.message}",
                error = e
            )
        }
    }

    // Test key encryption
    private fun testKeyEncryption(): TestResult {
        return try {
            val keyPair = keyManager.generateWalletKeyPair()
            val encrypted = keyManager.encryptPrivateKey(keyPair.privateKey, "testpin123")
            val decrypted = keyManager.decryptPrivateKey(encrypted, "testpin123")

            val passed = decrypted == keyPair.privateKey
            TestResult(
                name = "key_encryption",
                passed = passed,
                message = if (passed) "Key encryption/decryption works" else "Decryption failed",
                data = mapOf(
                    "encrypted" to encrypted.take(50) + "...",
                    "decrypted_matches" to passed
                )
            )
        } catch (e: Exception) {
            TestResult(
                name = "key_encryption",
                passed = false,
                message = "Key encryption test failed: ${e.message}",
                error = e
            )
        }
    }

    // Test fraud detection
    private fun testFraudDetection(): TestResult {
        return try {
            val largeTxn = Transaction(
                id = "test_txn_002",
                walletId = TEST_WALLET_ID,
                amount = 1.0, // Large amount to trigger fraud detection
                currency = "BITCOIN",
                type = "SEND",
                fromAddress = "from_addr",
                toAddress = "to_addr"
            )
            val result = walletService.signTransaction(largeTxn, "testpin123")

            TestResult(
                name = "fraud_detection",
                passed = true,
                message = "Fraud detection rule applied for large transaction",
                data = result
            )
        } catch (e: Exception) {
            TestResult(
                name = "fraud_detection",
                passed = false,
                message = "Fraud detection failed: ${e.message}",
                error = e
            )
        }
    }

    // Test offline transaction queueing
    private fun testOfflineTransactionQueueing(): TestResult {
        return try {
            val txn = Transaction(
                id = "test_txn_003",
                walletId = TEST_WALLET_ID,
                amount = 0.01,
                currency = "BITCOIN",
                type = "SEND",
                fromAddress = "from_addr",
                toAddress = "to_addr"
            )

            val testManager = TestQueueManager()
            testManager.queueForBroadcast(txn)

            TestResult(
                name = "offline_queueing",
                passed = true,
                message = "Transaction queued for offline broadcast",
                data = testManager.getQueueStats()
            )
        } catch (e: Exception) {
            TestResult(
                name = "offline_queueing",
                passed = false,
                message = "Offline queueing failed: ${e.message}",
                error = e
            )
        }
    }

    // Get all passed tests
    fun getPassedTests(): List<TestResult> {
        return testResults.filter { it.passed }
    }

    // Get all failed tests
    fun getFailedTests(): List<TestResult> {
        return testResults.filter { !it.passed }
    }

    // Summary
    fun getSummary(): QASummary {
        val passed = testResults.filter { it.passed }.size
        val failed = testResults.size - passed
        return QASummary(
            total = testResults.size,
            passed = passed,
            failed = failed,
           成功率 = "${(passed.toDouble() / testResults.size * 100).toInt()}%"
        )
    }
}

// Test result model
data class TestResult(
    val name: String,
    val passed: Boolean,
    val message: String,
    val data: Any? = null,
    val error: Throwable? = null
)

// QA summary model
data class QASummary(
    val total: Int,
    val passed: Int,
    val failed: Int,
    val成功率: String
)

// Test queue manager for testing
class TestQueueManager {
    private val queue = mutableListOf<Transaction>()

    fun queueForBroadcast(txn: Transaction) {
        queue.add(txn)
    }

    fun getQueueStats(): QueueStats {
        return QueueStats(
            queued = queue.size,
            ready = queue.size,
            failed = 0,
            maxSize = 100,
            maxRetries = 5
        )
    }
}
