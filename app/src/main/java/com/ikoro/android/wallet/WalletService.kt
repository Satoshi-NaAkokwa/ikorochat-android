package com.ikoro.android.wallet

import android.content.Context
import com.ikoro.android.IkoroApplication
import com.ikoro.android.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.SegwitAddress
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionConfidence
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.KeyChain
import org.bitcoinj.wallet.KeyChainGroup

/**
 * Simple embedded wallet using bitcoinj.
 *
 * Uses the BIP84 account key derived from the Ikoro identity seed.
 * Supports Native SegWit (bech32) addresses.
 *
 * This is a read-only / single-address MVP. It does not sync the chain.
 * Balance and UTXO data must come from a backend or an external wallet service in production.
 */
class WalletService private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: WalletService? = null

        fun getInstance(context: Context): WalletService {
            return instance ?: synchronized(this) {
                instance ?: WalletService(context.applicationContext).also { instance = it }
            }
        }
    }

    private val identityManager = IdentityManager(context)
    private val network = TestNet3Params.get() // Use testnet for MVP safety

    private val accountKey: DeterministicKey?
        get() = identityManager.getBitcoinAccountKey()

    /**
     * Current receive address (Native SegWit / bech32).
     */
    fun getReceiveAddress(): String? {
        val key = accountKey ?: return null
        return SegwitAddress.fromKey(network, key).toString()
    }

    /**
     * Get a fresh address at a specific BIP84 index.
     */
    fun getAddress(index: Int): String? {
        val key = accountKey?.let { parent ->
            org.bitcoinj.crypto.HDKeyDerivation.deriveChildKey(parent, index)
        } ?: return null
        return SegwitAddress.fromKey(network, key).toString()
    }

    /**
     * Derive the private key for a given address index.
     */
    fun getPrivateKeyForIndex(index: Int): ECKey? {
        return accountKey?.let { parent ->
            org.bitcoinj.crypto.HDKeyDerivation.deriveChildKey(parent, index)
        }
    }

    /**
     * Decode a BIP21 / plain address.
     */
    fun parseAddress(address: String): Address? {
        return try {
            Address.fromString(network, address)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Build an unsigned transaction sending [amountSats] to [destinationAddress].
     * Returns null if we can't build it. In a real implementation this needs UTXOs.
     */
    fun buildTransaction(
        destinationAddress: String,
        amountSats: Long,
        feeSats: Long
    ): Transaction? {
        val address = parseAddress(destinationAddress) ?: return null
        val tx = Transaction(network)
        tx.addOutput(Coin.valueOf(amountSats), address)
        // TODO: add inputs from UTXO set and sign
        return tx
    }

    /**
     * Placeholder balance until UTXO sync is wired up.
     */
    fun getBalance(): WalletBalance {
        return WalletBalance(0L, 0L, 0L)
    }
}

data class WalletBalance(
    val totalSats: Long,
    val confirmedSats: Long,
    val pendingSats: Long
)
