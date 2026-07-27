package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "LK")).apply {
        currency = java.util.Currency.getInstance("LKR")
        maximumFractionDigits = 2
    }

    fun formatDate(timestampMs: Long): String {
        return dateFormatter.format(Date(timestampMs))
    }

    fun formatCurrency(amount: Double): String {
        return "LKR ${String.format(Locale.US, "%,.2f", amount)}"
    }

    fun calculateDays(startMs: Long, endMs: Long): Int {
        val diff = (endMs - startMs).coerceAtLeast(0)
        val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
        return if (days <= 0) 1 else days
    }

    fun getTodayStartMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getTodayEndMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun generateBillNumber(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        val random = (100..999).random()
        return "INV-$dateStr-$random"
    }

    fun generateQuoteNumber(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        val random = (100..999).random()
        return "QTE-$dateStr-$random"
    }
}
