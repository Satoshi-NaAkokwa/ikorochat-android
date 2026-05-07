//
// WalletIntelligenceService.kt
// Ikoro - ₿ỌFỌ Platform
//
// AI-powered wallet intelligence features
//

package com.ikoro.android.ai.services

import android.content.Context
import com.ikoro.android.ai.AgbaraAssistant
import com.ikoro.android.ecommerce.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Wallet Intelligence Service - AI-powered financial insights
 */
class WalletIntelligenceService(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Analyze spending trends
     */
    suspend fun analyzeSpendingTrends(transactions: List<Transaction>, currency: AgbaraAssistant.Language): String = withContext(Dispatchers.IO) {
        if (transactions.isEmpty()) return@withContext "No transactions to analyze"

        val sentTransactions = transactions.filter { it.type == Transaction.TransactionType.SEND }
        val receivedTransactions = transactions.filter { it.type == Transaction.TransactionType.RECEIVE }

        val totalSent = sentTransactions.sumOf { it.amount }
        val totalReceived = receivedTransactions.sumOf { it.amount }

        // Calculate weekly average
        val uniqueWeeks = transactions.map { getWeekNumber(it.timestamp) }.distinct().size
        val avgWeeklySpending = totalSent / uniqueWeeks.coerceAtLeast(1)

        """
        💰 Spending Analysis (${currency.displayName})

        Total Sent: ₿${String.format("%.8f", totalSent)}
        Total Received: ₿${String.format("%.8f", totalReceived)}
        Net Flow: ₿${String.format("%.8f", totalReceived - totalSent)}

        Average Weekly Spending: ₿${String.format("%.8f", avgWeeklySpending)}

        📊 Insights:
        • Transactions analyzed: ${transactions.size}
        • Weeks analyzed: $uniqueWeeks
        ${if (totalReceived > totalSent) "✅ Positive net flow - you're earning more than spending" else "⚠️ Negative net flow - consider reducing spending"}
        """.trimIndent()
    }

    /**
     * Recommend budget allocation
     */
    suspend fun recommendBudget(monthlyIncome: Double, currency: String): String = withContext(Dispatchers.IO) {
        val savings = monthlyIncome * 0.20 // 20% savings
        val essentials = monthlyIncome * 0.50 // 50% essentials
        val discretionary = monthlyIncome * 0.30 // 30% discretionary

        """
        📋 Budget Recommendation (Monthly Income: ₿${String.format("%.8f", monthlyIncome)})

        💰 Savings (20%): ₿${String.format("%.8f", savings)}
        🏠 Essentials (50%): ₿${String.format("%.8f", essentials)}
        🛍️ Discretionary (30%): ₿${String.format("%.8f", discretionary)}

        💡 Tips:
        • Keep your ₿ỌFỌ balance for everyday transactions
        • Use ₿ for long-term savings
        • Save in ₦ for local purchases when available
        """.trimIndent()
    }

    /**
     * Detect unusual transaction patterns
     */
    suspend fun detectAnomalies(transactions: List<Transaction>): List<String> = withContext(Dispatchers.IO) {
        if (transactions.size < 5) return@withContext emptyList()

        val anomalies = ArrayList<String>()
        val amounts = transactions.map { it.amount }

        // Calculate average and standard deviation
        val avg = amounts.average()
        val stdDev = calculateStandardDeviation(amounts)

        // Flag transactions more than 2 standard deviations from mean
        transactions.forEach { tx ->
            if (abs(tx.amount - avg) > 2 * stdDev) {
                anomalies.add("Unusual transaction: ${tx.description} for ₿${String.format("%.8f", tx.amount)}")
            }
        }

        anomalies
    }

    /**
     * Get currency exchange recommendations
     */
    suspend fun getExchangeRecommendations(currency: AgbaraAssistant.Currency): String = withContext(Dispatchers.IO) {
        val recommendations = when (currency) {
            AgbaraAssistant.Currency.BITCOIN -> "Hold for long-term investment"
            AgbaraAssistant.Currency.OFO -> "Use for everyday transactions - lowest fees"
            AgbaraAssistant.Currency.NAIRA -> "Use for local purchases when internet available"
            AgbaraAssistant.Currency.USDT -> "Use for stable value preservation"
            AgbaraAssistant.Currency.USDC -> "Use for international transactions"
        }

        """
        💡 ${currency.name} Recommendations:

        $recommendations

        Current exchange rates (cached):
        1 ₿ = ₦${String.format("%,.2f", 45000000.0)}
        1 ₿ỌFỌ = ₦${String.format("%,.2f", 100.0)}
        """.trimIndent()
    }

    /**
     * Calculate standard deviation
     */
    private fun calculateStandardDeviation(values: List<Double>): Double {
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }

    /**
     * Get week number from timestamp
     */
    private fun getWeekNumber(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }
}