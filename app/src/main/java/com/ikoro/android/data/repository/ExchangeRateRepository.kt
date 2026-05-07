package com.ikoro.android.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.ikoro.android.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Repository for managing exchange rates with offline caching support
 */
class ExchangeRateRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private const val PREFS_NAME = "exchange_rates_prefs"
        private const val KEY_CACHED_RATES = "cached_exchange_rates"
        private const val DEFAULT_CACHE_DURATION = 3600000L // 1 hour in milliseconds
    }

    /**
     * Get cached exchange rates
     */
    fun getCachedRates(): CachedExchangeRates? {
        return try {
            val ratesJson = prefs.getString(KEY_CACHED_RATES, null)
            if (ratesJson != null) {
                json.decodeFromString<CachedExchangeRates>(ratesJson)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cache exchange rates
     */
    fun cacheRates(rates: List<ExchangeRate>, cacheDuration: Long = DEFAULT_CACHE_DURATION) {
        try {
            val ratesMap = mutableMapOf<String, ExchangeRate>()
            rates.forEach { rate ->
                val key = "${rate.fromCurrency.name}_to_${rate.toCurrency.name}"
                ratesMap[key] = rate
            }

            val cachedRates = CachedExchangeRates(
                rates = ratesMap,
                lastUpdated = System.currentTimeMillis(),
                cacheDuration = cacheDuration
            )

            val ratesJson = json.encodeToString(cachedRates)
            prefs.edit()
                .putString(KEY_CACHED_RATES, ratesJson)
                .apply()
        } catch (e: Exception) {
            // Handle serialization error
        }
    }

    /**
     * Get a specific exchange rate (from cache)
     */
    fun getExchangeRate(from: Currency, to: Currency): ExchangeRate? {
        val cachedRates = getCachedRates() ?: return null
        return cachedRates.getRate(from, to)
    }

    /**
     * Check if cached rates are expired
     */
    fun isCacheExpired(): Boolean {
        val cachedRates = getCachedRates() ?: return true
        return cachedRates.isExpired()
    }

    /**
     * Clear cached exchange rates
     */
    fun clearCache() {
        prefs.edit()
            .remove(KEY_CACHED_RATES)
            .apply()
    }

    /**
     * Get sample/demo exchange rates for testing
     */
    fun getSampleRates(): List<ExchangeRate> {
        return listOf(
            ExchangeRate(Currency.BITCOIN, Currency.USDT, 65000.0, System.currentTimeMillis()),
            ExchangeRate(Currency.BITCOIN, Currency.USDC, 65050.0, System.currentTimeMillis()),
            ExchangeRate(Currency.BITCOIN, Currency.NAIRA, 97500000.0, System.currentTimeMillis()),
            ExchangeRate(Currency.BITCOIN, Currency.OFO, 1.0, System.currentTimeMillis()), // 1 BTC = 1 OFO (example)
            ExchangeRate(Currency.USDT, Currency.BITCOIN, 0.00001538, System.currentTimeMillis()),
            ExchangeRate(Currency.USDT, Currency.USDC, 1.00015, System.currentTimeMillis()),
            ExchangeRate(Currency.USDT, Currency.NAIRA, 1500.0, System.currentTimeMillis()),
            ExchangeRate(Currency.USDC, Currency.BITCOIN, 0.00001537, System.currentTimeMillis()),
            ExchangeRate(Currency.USDC, Currency.NAIRA, 1499.85, System.currentTimeMillis()),
            ExchangeRate(Currency.NAIRA, Currency.BITCOIN, 0.00000001025, System.currentTimeMillis()),
            ExchangeRate(Currency.NAIRA, Currency.USDT, 0.000666, System.currentTimeMillis()),
            ExchangeRate(Currency.NAIRA, Currency.USDC, 0.0006667, System.currentTimeMillis()),
            ExchangeRate(Currency.OFO, Currency.BITCOIN, 1.0, System.currentTimeMillis()),
            ExchangeRate(Currency.OFO, Currency.USDT, 65000.0, System.currentTimeMillis()),
        )
    }

    /**
     * Initialize cache with sample rates (for demo purposes)
     */
    fun initializeWithSampleRates() {
        if (getCachedRates() == null) {
            val sampleRates = getSampleRates()
            cacheRates(sampleRates)
        }
    }

    /**
     * Convert amount from one currency to another using cached rates
     */
    fun convert(
        amount: Double,
        from: Currency,
        to: Currency
    ): Double? {
        if (from == to) return amount

        // Try direct conversion
        val directRate = getExchangeRate(from, to)
        if (directRate != null) {
            return directRate.convert(amount)
        }

        // Try conversion through Bitcoin as intermediary
        val toBtc = getExchangeRate(from, Currency.BITCOIN)
        val fromBtc = getExchangeRate(Currency.BITCOIN, to)

        if (toBtc != null && fromBtc != null) {
            val btcAmount = toBtc.convert(amount)
            return fromBtc.convert(btcAmount)
        }

        return null
    }

    /**
     * Get all available rates from cache
     */
    fun getAllRates(): List<ExchangeRate> {
        val cachedRates = getCachedRates() ?: return emptyList()
        return cachedRates.rates.values.toList()
    }
}