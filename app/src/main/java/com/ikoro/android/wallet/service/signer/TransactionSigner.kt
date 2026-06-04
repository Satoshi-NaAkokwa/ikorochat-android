package com.ikoro.android.wallet.service.signer

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.ikoro.android.crypto.SigningUtils
import com.ikoro.android.data.model.Currency
import com.ikoro.android.wallet.model.CoinData
import com.ikoro.android.wallet.model.TransactionEntity
import com.ikoro.android.wallet.model.WalletEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.params.MainNetParams
import org.bitcoinj.core.params.TestNetParams
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptChunk
import java.util.concurrent.TimeUnit

/**
 * TransactionSigner service for offline transaction signing
 * Uses Android keystore for key protection and supports multiple currencies
 */
class TransactionSigner(private val context: Context) {
    
    private val TAG = "TransactionSigner"
    
    // Cryptographic utilities
    private val signingUtils = SigningUtils
    private val keyManager = KeyManager(context)
    
    /**
     * Sign a Bitcoin transaction offline
     * Returns signed transaction bytes or null if signing fails
     */
    suspend fun signBitcoinTransaction(
        wallet: WalletEntity,
        transactionData: TransactionData,
        inputs: List<TransactionInput>
    ): TransactionSignResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting Bitcoin transaction signing for ${wallet.currency}")
                
                // Get private key from keystore
                val privateKeyData = keyManager.getPrivateKey(wallet.currency)
                if (privateKeyData == null) {
                    return@withContext TransactionSignResult.Error(
                        "Private key not found for ${wallet.currency}"
                    )
                }
                
                val params = getNetworkParams(wallet.coinData?.network ?: "mainnet")
                
                // Build transaction
                val tx = Transaction(params)
                tx.version = 1
                
                // Add inputs
                inputs.forEach { input ->
                    val inputTx = tx.addInput(input.previousTxId, input.vout, null)
                    inputTx.sequenceNumber = input.sequenceNumber
                }
                
                // Add outputs
                transactionData.outputs.forEach { output ->
                    val script = ScriptBuilder.createOutputScript(output.address)
                    tx.addOutput(output.amountSatoshis, script)
                }
                
                // Add change output if specified
                transactionData.changeAddress?.let { changeAddr ->
                    val changeScript = ScriptBuilder.createOutputScript(changeAddr)
                    val changeAmount = transactionData.fee.toLong()
                    tx.addOutput(changeAmount, changeScript)
                }
                
                // Sign inputs
                val privKey = ECKey.fromPrivKey(privateKeyData)
                inputs.forEachIndexed { index, input ->
                    val scriptBytes = input.scriptPubKey?.let { Script(it).scriptBytes } ?: byteArrayOf()
                    tx.signInput(index, privKey, scriptBytes, input.valueSatoshis, Transaction.SigHash.ALL)
                }
                
                val signedTxBytes = tx.bitcoinSerialize()
                
                Log.d(TAG, "Successfully signed transaction: ${tx.hashAsString}")
                
                TransactionSignResult.Success(
                    signedTxBytes,
                    tx.hashAsString,
                    tx.inputs.size,
                    tx.outputs.size
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sign Bitcoin transaction", e)
                TransactionSignResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Sign a Stacks (OFO) transaction offline
     */
    suspend fun signStacksTransaction(
        wallet: WalletEntity,
        transactionData: StacksTransactionData
    ): TransactionSignResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting Stacks transaction signing")
                
                // Get private key from keystore
                val privateKeyData = keyManager.getPrivateKey(wallet.currency)
                if (privateKeyData == null) {
                    return@withContext TransactionSignResult.Error(
                        "Private key not found for ${wallet.currency}"
                    )
                }
                
                // Use Stacks transaction signing (simplified)
                val signature = signingUtils.signData(
                    privateKeyData,
                    transactionData.transactionHash.toByteArray()
                )
                
                TransactionSignResult.Success(
                    transactionData.transactionHash.toByteArray(),
                    transactionData.transactionHash,
                    1,
                    transactionData.amount.toString().length
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sign Stacks transaction", e)
                TransactionSignResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Sign a Naira transaction offline
     */
    suspend fun signNairaTransaction(
        wallet: WalletEntity,
        transactionData: NairaTransactionData
    ): TransactionSignResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting Naira transaction signing")
                
                val signature = signingUtils.signData(
                    transactionData.amount.toString().toByteArray(),
                    transactionData.timestamp.toString().toByteArray()
                )
                
                TransactionSignResult.Success(
                    signature,
                    "TX_${transactionData.timestamp}",
                    1,
                    1
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sign Naira transaction", e)
                TransactionSignResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Sign a USDT (TRC20) transaction offline
     */
    suspend fun signUsdtTransaction(
        wallet: WalletEntity,
        transactionData: UsdtTransactionData
    ): TransactionSignResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting USDT transaction signing")
                
                val privateKeyData = keyManager.getPrivateKey(wallet.currency)
                if (privateKeyData == null) {
                    return@withContext TransactionSignResult.Error(
                        "Private key not found for ${wallet.currency}"
                    )
                }
                
                // TRC20 signing
                val signature = signingUtils.signData(
                    privateKeyData,
                    transactionData.contractAddress.toByteArray() + transactionData.amount.toByteArray()
                )
                
                TransactionSignResult.Success(
                    signature,
                    transactionData.hash,
                    1,
                    1
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sign USDT transaction", e)
                TransactionSignResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Sign a USDC transaction offline
     */
    suspend fun signUsdcTransaction(
        wallet: WalletEntity,
        transactionData: UsdcTransactionData
    ): TransactionSignResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting USDC transaction signing")
                
                val privateKeyData = keyManager.getPrivateKey(wallet.currency)
                if (privateKeyData == null) {
                    return@withContext TransactionSignResult.Error(
                        "Private key not found for ${wallet.currency}"
                    )
                }
                
                // Ethereum/EVM signature
                val signature = signingUtils.signData(
                    privateKeyData,
                    transactionData.eip712Domain.toByteArray()
                )
                
                TransactionSignResult.Success(
                    signature,
                    transactionData.hash,
                    1,
                    1
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sign USDC transaction", e)
                TransactionSignResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Verify a transaction signature
     */
    suspend fun verifySignature(
        publicKey: ByteArray,
        transactionHash: String,
        signature: ByteArray
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                signingUtils.verifySignature(publicKey, transactionHash.toByteArray(), signature)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to verify signature", e)
                false
            }
        }
    }
    
    /**
     * Clean up signer resources
     */
    fun cleanup() {
        keyManager.cleanup()
    }
    
    // Network helpers
    
    private fun getNetworkParams(network: String): com.bitcoinj.core.NetworkParameters {
        return when (network.lowercase()) {
            "mainnet" -> MainNetParams()
            "testnet", "test" -> TestNetParams()
            else -> MainNetParams()
        }
    }
}

/**
 * Data class for transaction input
 */
data class TransactionInput(
    val previousTxId: com.bitcoinj.core.Transaction.PrevOut,
    val vout: Int,
    val scriptPubKey: ByteArray?,
    val valueSatoshis: Long,
    val sequenceNumber: Int = 0xFFFFFFFF
)

/**
 * Data class for transaction output
 */
data class TransactionOutput(
    val address: String,
    val amountSatoshis: Long
)

/**
 * Data class for Bitcoin transaction data
 */
data class TransactionData(
    val outputs: List<TransactionOutput>,
    val fee: Long,
    val changeAddress: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val lockTime: Int = 0
)

/**
 * Data class for Stacks (OFO) transaction data
 */
data class StacksTransactionData(
    val recipient: String,
    val amount: Long,
    val feeRate: Long,
    val nonce: Long,
    val publicKey: String,
    val transactionHash: String,
    val memo: String? = null
)

/**
 * Data class for Naira transaction data
 */
data class NairaTransactionData(
    val recipient: String,
    val amount: Double,
    val currency: String = "NGN",
    val timestamp: Long = System.currentTimeMillis(),
    val reference: String = "",
    val narration: String? = null,
    val channel: String = "mobile"
)

/**
 * Data class for USDT (TRC20) transaction data
 */
data class UsdtTransactionData(
    val recipient: String,
    val contractAddress: String,
    val amount: Long,
    val feeLimit: Long,
    val callValue: Long,
    val hash: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class for USDC (EVM) transaction data
 */
data class UsdcTransactionData(
    val recipient: String,
    val contractAddress: String,
    val amount: String,
    val gasLimit: Long,
    val gasPrice: String,
    val nonce: Long,
    val eip712Domain: String,
    val hash: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Result of transaction signing
 */
sealed class TransactionSignResult {
    data class Success(
        val signedData: ByteArray,
        val transactionHash: String,
        val inputCount: Int,
        val outputCount: Int
    ) : TransactionSignResult()
    
    data class Error(val message: String) : TransactionSignResult()
}
