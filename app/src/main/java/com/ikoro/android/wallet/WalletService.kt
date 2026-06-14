package com.ikoro.android.wallet

import android.content.Context
import breez_sdk_liquid.*
import com.ikoro.android.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Breez SDK - Liquid wallet service.
 */
class WalletService(
    private val context: Context,
    private val identityManager: IdentityManager
) {
    private var sdk: BindingLiquidSdk? = null

    private val apiKey: String = com.ikoro.android.BuildConfig.BREEZ_API_KEY
        .takeIf { it.isNotBlank() }
        ?: """MIIBhjCCATigAwIBAgIHPww+WsFibTAFBgMrZXAwEDEOMAwGA1UEAxMFQnJlZXowHhcNMjYwMzI3MTkwOTA4WhcNMzYwMzI0MTkwOTA4WjA5MRwwGgYDVQQKExNPRk9OT0dVIEFTU0VUUyAgTFREMRkwFwYDVQQDExBDSElORU5ZRSBJQkVNRVJFMCowBQYDK2VwAyEA0IP1y98gPByiIMoph1P0G6cctLb864rNXw1LRLOpXXejgYcwgYQwDgYDVR0PAQH/BAQDAgWgMAwGA1UdEwEB/wQCMAAwHQYDVR0OBBYEFNo5o+5ea0sNMlW/75VgGJCv2AcJMB8GA1UdIwQYMBaAFN6q1pJW843ndJIW/Ey2ILJrKJhrMCQGA1UdEQQdMBuBGWNoaW5lbnllQHd3dy5vZm9uYW9ndS5jb20wBQYDK2VwA0EA0pOOlFYJbylIfMtiSkKk4/RTQgV0z4sSL2SIQliQLVgGKgiifDVgJimKEgL+YCt7ICE+S5SFsfKX3HhT8tsFDg==""".trimIndent()

    private val _state = MutableStateFlow<WalletState>(WalletState.NotInitialized)
    val state: StateFlow<WalletState> = _state

    private val _balanceSat = MutableStateFlow(0L)
    val balanceSat: StateFlow<Long> = _balanceSat

    private val _balanceUsdt = MutableStateFlow(0L)
    val balanceUsdt: StateFlow<Long> = _balanceUsdt

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun isApiKeyValid(): Boolean = apiKey.length in 20..200 && !apiKey.contains("BEGIN CERTIFICATE")

    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isApiKeyValid()) {
                _error.value = "Breez API key appears invalid. Provide a short API key in local.properties, not a certificate."
                return@withContext Result.failure(IllegalStateException("Breez API key appears invalid. Provide a short API key in local.properties, not a certificate."))
            }
            _state.value = WalletState.Connecting
            val mnemonic = identityManager.getMnemonic()
                ?: return@withContext Result.failure(IllegalStateException("No mnemonic"))

            val workingDir = File(context.filesDir, "breez_liquid").apply { mkdirs() }.absolutePath
            val config: Config = defaultConfig(LiquidNetwork.MAINNET, apiKey)
            config.workingDir = workingDir

            val request = ConnectRequest(config, mnemonic)
            sdk = connect(request)

            _state.value = WalletState.Ready
            refreshBalance()
            Result.success(Unit)
        } catch (e: Exception) {
            _error.value = e.message ?: "Breez connect failed"
            _state.value = WalletState.Error(e.message ?: "Breez connect failed")
            Result.failure(e)
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            sdk?.disconnect()
        } catch (_: Exception) { }
        sdk = null
        _state.value = WalletState.NotInitialized
    }

    suspend fun refreshBalance() = withContext(Dispatchers.IO) {
        try {
            val walletInfo = sdk?.getInfo()?.walletInfo
            _balanceSat.value = walletInfo?.balanceSat?.toLong() ?: 0L
            // Breez Liquid stablecoin balances are listed per asset
            val usdtAssetId = "6f0279e9ed041c3d710a9f57d0c02928416460c4b722ae3457a11eec381c526d"
            val usdtBalance = walletInfo?.assetBalances?.find { it.assetId == usdtAssetId }?.balance?.toLong() ?: 0L
            _balanceUsdt.value = usdtBalance
            listPayments().onSuccess { _payments.value = it }
        } catch (e: Exception) {
            _state.value = WalletState.Error(e.message ?: "Balance sync failed")
        }
    }

    suspend fun receivePayment(
        payerAmountSat: Long?,
        asset: Asset = Asset.BTC,
        description: String = "Ikoro payment"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val amount = payerAmountSat?.let {
                when (asset) {
                    Asset.BTC -> breez_sdk_liquid.ReceiveAmount.Bitcoin(it.toULong())
                    Asset.LUSDT -> {
                        // L-USDT amount requires the correct asset decimal handling.
                        // Using amount-less invoice for now to keep compile/runtime safe.
                        null
                    }
                }
            }
            val req = PrepareReceiveRequest(
                paymentMethod = PaymentMethod.BOLT11_INVOICE,
                amount = amount
            )
            val prepareResponse = sdk?.prepareReceivePayment(req)
                ?: return@withContext Result.failure(IllegalStateException("SDK not connected"))
            val response = sdk?.receivePayment(
                ReceivePaymentRequest(
                    prepareResponse = prepareResponse,
                    description = description
                )
            )
            Result.success(response?.destination ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPayment(destination: String, asset: Asset = Asset.BTC): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = PrepareSendRequest(destination = destination)
            val prepare = sdk?.prepareSendPayment(req)
                ?: return@withContext Result.failure(IllegalStateException("SDK not connected"))
            val response = sdk?.sendPayment(SendPaymentRequest(prepareResponse = prepare))
            Result.success(response?.payment?.txId ?: "pending")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listPayments(): Result<List<Payment>> = withContext(Dispatchers.IO) {
        try {
            val req = ListPaymentsRequest()
            val list = sdk?.listPayments(req)
            Result.success(list ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun payToNpub(npubHex: String, amountSat: Long, asset: Asset = Asset.BTC): Result<String> = withContext(Dispatchers.IO) {
        // Placeholder: resolve npub -> Liquid address via nostr profile or contact registry
        Result.failure(UnsupportedOperationException("npub-to-address resolution not implemented yet"))
    }
}

enum class Asset { BTC, LUSDT }

sealed class WalletState {
    object NotInitialized : WalletState()
    object Connecting : WalletState()
    object Ready : WalletState()
    data class Error(val message: String) : WalletState()
}