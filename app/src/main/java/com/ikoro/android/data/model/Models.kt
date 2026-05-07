package com.ikoro.android.data.model

import kotlinx.serialization.Serializable

/**
 * Currency balance for a specific currency type
 */
@Serializable
data class CurrencyBalance(
    val currency: Currency,
    val amount: Double,
    val symbol: String = when (currency) {
        Currency.BITCOIN -> "₿"
        Currency.OFO -> "₿ỌFỌ"
        Currency.NAIRA -> "₦"
        Currency.USDT -> "USDT"
        Currency.USDC -> "USDC"
    },
    val decimals: Int = when (currency) {
        Currency.BITCOIN -> 8
        Currency.OFO -> 8
        Currency.NAIRA -> 2
        Currency.USDT -> 6
        Currency.USDC -> 6
    },
    val icon: String = when (currency) {
        Currency.BITCOIN -> "₿"
        Currency.OFO -> "₿ỌFỌ"
        Currency.NAIRA -> "₦"
        Currency.USDT -> "₮"
        Currency.USDC -> "₿"
    }
) {
    fun formatAmount(): String = when (currency) {
        Currency.BITCOIN -> "${symbol}%.${decimals}f".format(amount)
        Currency.OFO -> "${symbol}%.${decimals}f".format(amount)
        Currency.NAIRA -> "${symbol}%.${decimals}f".format(amount)
        Currency.USDT -> "${symbol}%.${decimals}f".format(amount)
        Currency.USDC -> "${symbol}%.${decimals}f".format(amount)
    }
}

/**
 * Exchange rate between two currencies
 */
@Serializable
data class ExchangeRate(
    val fromCurrency: Currency,
    val toCurrency: Currency,
    val rate: Double,
    val timestamp: Long
) {
    fun convert(amount: Double): Double = amount * rate
}

/**
 * Multi-currency wallet containing balances for all supported currencies
 */
@Serializable
data class MultiCurrencyWallet(
    val balances: Map<Currency, CurrencyBalance> = mapOf(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun getBalance(currency: Currency): CurrencyBalance? = balances[currency]

    fun getTotalBalanceIn(baseCurrency: Currency, rates: List<ExchangeRate>): Double {
        return balances.values.sumOf { balance ->
            if (balance.currency == baseCurrency) {
                balance.amount
            } else {
                rates.find {
                    it.fromCurrency == balance.currency && it.toCurrency == baseCurrency
                }?.convert(balance.amount) ?: 0.0
            }
        }
    }

    fun toDisplayString(): String {
        val primaryBalance = balances[Currency.BITCOIN]?.formatAmount() ?: "₿0.00000000"
        val ofoBalance = balances[Currency.OFO]?.formatAmount() ?: "₿ỌFỌ0.00000000"
        val nairaBalance = balances[Currency.NAIRA]?.formatAmount() ?: "₦0.00"
        val usdtBalance = balances[Currency.USDT]?.formatAmount() ?: "USDT0.000000"
        val usdcBalance = balances[Currency.USDC]?.formatAmount() ?: "USDC0.000000"

        return """
            $primaryBalance
            $ofoBalance
            $nairaBalance
            $usdtBalance
            $usdcBalance
        """.trimIndent()
    }
}

/**
 * Transaction for any currency type
 */
@Serializable
data class CurrencyTransaction(
    val id: String,
    val description: String,
    val amount: Double,
    val currency: Currency,
    val type: TransactionType,
    val timestamp: Long,
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

/**
 * Transaction type
 */
enum class TransactionType {
    SEND,
    RECEIVE,
    EXCHANGE
}

/**
 * Transaction status
 */
enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}

/**
 * Cached exchange rates for offline use
 */
@Serializable
data class CachedExchangeRates(
    val rates: Map<String, ExchangeRate> = mapOf(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val cacheDuration: Long = 3600000 // 1 hour in milliseconds
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - lastUpdated > cacheDuration
    }

    fun getRate(from: Currency, to: Currency): ExchangeRate? {
        val key = "${from.name}_to_${to.name}"
        return rates[key]
    }

    fun addRate(rate: ExchangeRate): CachedExchangeRates {
        val key = "${rate.fromCurrency.name}_to_${rate.toCurrency.name}"
        return copy(
            rates = rates + (key to rate),
            lastUpdated = System.currentTimeMillis()
        )
    }
}

/**
 * Payment QR code data
 */
@Serializable
data class PaymentQRCode(
    val id: String,
    val address: String,
    val currency: Currency,
    val amount: Double? = null,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Scheduled exchange order
 */
@Serializable
data class ExchangeOrder(
    val id: String,
    val fromCurrency: Currency,
    val toCurrency: Currency,
    val fromAmount: Double,
    val toCurrencyAmount: Double,
    val exchangeRate: Double,
    val status: ExchangeOrderStatus,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Exchange order status
 */
enum class ExchangeOrderStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}