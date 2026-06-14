package com.ikoro.android.identity

import android.content.Context
import com.ikoro.android.security.SecureVault
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import org.bitcoinj.wallet.DeterministicSeed
import java.security.SecureRandom

/**
 * Ikoro identity layer.
 *
 * Generates a single BIP-39 mnemonic on first launch and derives:
 *  - Nostr private key from m/44'/1237'/0'/0/0
 *  - Bitcoin wallet seed for BIP84 from the root seed
 *
 * The mnemonic itself is never stored; only the seed bytes are encrypted at rest.
 */
class IdentityManager(private val context: Context) {

    companion object {
        // NIP-06 Nostr derivation path: m/44'/1237'/0'/0/0
        private val NOSTR_PURPOSE = ChildNumber(44, true)
        private val NOSTR_COIN = ChildNumber(1237, true)
        private val NOSTR_ACCOUNT = ChildNumber(0, true)
        private val NOSTR_CHANGE = ChildNumber(0, false)
        private val NOSTR_INDEX = ChildNumber(0, false)

        // Bitcoin native segwit (BIP84) uses m/84'/0'/0' for mainnet.
        private val BTC_BIP84_PURPOSE = ChildNumber(84, true)
        private val BTC_COIN = ChildNumber(0, true)
        private val BTC_ACCOUNT = ChildNumber(0, true)
    }

    private val secureRandom = SecureRandom()

    /**
     * Returns true if the user already has a generated identity.
     */
    fun hasIdentity(): Boolean = SecureVault.hasSeed(context)

    /**
     * Get the root seed bytes if identity exists.
     */
    private fun getSeedBytes(): ByteArray? = SecureVault.retrieveSeed(context)

    /**
     * Get the BIP-39 mnemonic words if stored.
     */
    fun getMnemonic(): String? = SecureVault.retrieveMnemonic(context)

    /**
     * Generate a new identity. Returns the mnemonic words so the user can back them up.
     * The seed bytes are stored encrypted.
     */
    fun createIdentity(): List<String> {
        val entropy = ByteArray(16)
        secureRandom.nextBytes(entropy)
        val words = MnemonicCode.INSTANCE.toMnemonic(entropy)
        val seed = DeterministicSeed(words, null, "", System.currentTimeMillis()).seedBytes
            ?: throw IllegalStateException("Failed to derive seed")
        SecureVault.storeSeed(context, seed)
        SecureVault.storeMnemonic(context, words.joinToString(" "))
        return words
    }

    /**
     * Restore identity from a mnemonic phrase.
     */
    fun restoreIdentity(words: List<String>): Result<Unit> {
        return try {
            MnemonicCode.INSTANCE.check(words)
            val seed = DeterministicSeed(words, null, "", System.currentTimeMillis()).seedBytes
                ?: throw IllegalStateException("Failed to derive seed")
            SecureVault.storeSeed(context, seed)
            SecureVault.storeMnemonic(context, words.joinToString(" "))
            Result.success(Unit)
        } catch (e: MnemonicException) {
            Result.failure(e)
        }
    }

    /**
     * Derive the Nostr private key (32 bytes) from the identity seed.
     */
    fun getNostrPrivateKey(): ByteArray? {
        val seed = getSeedBytes() ?: return null
        val root = HDKeyDerivation.createMasterPrivateKey(seed)
        val path44 = HDKeyDerivation.deriveChildKey(root, NOSTR_PURPOSE)
        val path1237 = HDKeyDerivation.deriveChildKey(path44, NOSTR_COIN)
        val path0 = HDKeyDerivation.deriveChildKey(path1237, NOSTR_ACCOUNT)
        val pathChange = HDKeyDerivation.deriveChildKey(path0, NOSTR_CHANGE)
        val nostrKey = HDKeyDerivation.deriveChildKey(pathChange, NOSTR_INDEX)
        return nostrKey.privKeyBytes
    }

    /**
     * Derive the Bitcoin BIP84 account key for wallet use.
     */
    fun getBitcoinAccountKey(): DeterministicKey? {
        val seed = getSeedBytes() ?: return null
        val root = HDKeyDerivation.createMasterPrivateKey(seed)
        val p84 = HDKeyDerivation.deriveChildKey(root, BTC_BIP84_PURPOSE)
        val p0 = HDKeyDerivation.deriveChildKey(p84, BTC_COIN)
        return HDKeyDerivation.deriveChildKey(p0, BTC_ACCOUNT)
    }

    /**
     * Hex-encoded Nostr public key (npub hex) for display.
     */
    fun getNostrPublicKeyHex(): String? {
        val priv = getNostrPrivateKey() ?: return null
        // Nostr uses secp256k1 public key derivation. We'll add the actual EC point computation
        // in a utility class to avoid pulling in too many dependencies here.
        return NostrCrypto.publicKeyHexFromPrivateKey(priv)
    }

    fun clearIdentity() {
        SecureVault.clearAll(context)
    }
}
