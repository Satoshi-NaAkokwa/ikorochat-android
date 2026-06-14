package com.ikoro.android.identity

import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.bouncycastle.util.encoders.Hex

/**
 * Minimal Nostr cryptography helpers using BouncyCastle.
 *
 * Nostr private key is 32 bytes; public key is the corresponding secp256k1 X coordinate
 * encoded as 32-byte big-endian (hex encoded for display/events).
 */
object NostrCrypto {

    private val curveParams: X9ECParameters = org.bouncycastle.asn1.sec.SECNamedCurves.getByName("secp256k1")
    private val domainParams = ECDomainParameters(curveParams.curve, curveParams.g, curveParams.n, curveParams.h)

    /**
     * Compute the 32-byte public key (X coordinate only) from a 32-byte private key.
     */
    fun publicKeyBytesFromPrivateKey(privateKey: ByteArray): ByteArray {
        require(privateKey.size == 32) { "Private key must be 32 bytes" }
        val privBig = java.math.BigInteger(1, privateKey)
        val pubPoint = FixedPointCombMultiplier().multiply(domainParams.g, privBig)
        return pubPoint.affineXCoord.encoded
    }

    fun publicKeyHexFromPrivateKey(privateKey: ByteArray): String {
        return Hex.toHexString(publicKeyBytesFromPrivateKey(privateKey))
    }

    /**
     * Sign a 32-byte message hash with a Nostr private key using RFC 6979 deterministic ECDSA.
     */
    fun sign(privateKey: ByteArray, messageHash: ByteArray): ByteArray {
        require(privateKey.size == 32) { "Private key must be 32 bytes" }
        require(messageHash.size == 32) { "Message hash must be 32 bytes" }
        val privBig = java.math.BigInteger(1, privateKey)
        val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
        signer.init(true, ECPrivateKeyParameters(privBig, domainParams))
        val signature = signer.generateSignature(messageHash)
        val r = signature[0]
        val s = signature[1]
        // Nostr signatures are canonical low-s
        val canonicalS = if (s > domainParams.n.shiftRight(1)) domainParams.n.subtract(s) else s
        val rBytes = r.toByteArray().let { if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else it.copyOf(32) }
        val sBytes = canonicalS.toByteArray().let { if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else it.copyOf(32) }
        return rBytes + sBytes
    }

    fun sha256(data: ByteArray): ByteArray {
        val digest = SHA256Digest()
        digest.update(data, 0, data.size)
        val out = ByteArray(32)
        digest.doFinal(out, 0)
        return out
    }
}
