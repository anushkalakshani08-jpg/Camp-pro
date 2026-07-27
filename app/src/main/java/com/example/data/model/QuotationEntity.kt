package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotations")
data class QuotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteNumber: String,
    val customerName: String,
    val customerPhone: String,
    val customerNic: String = "",
    val startDate: Long,
    val endDate: Long,
    val totalDays: Int,
    val subtotal: Double,
    val discountType: String = "NONE", // NONE, FLAT, PERCENT
    val discountValue: Double = 0.0,
    val totalDeposit: Double,
    val grandTotal: Double,
    val isConvertedToBill: Boolean = false,
    val convertedBillId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quotation_items")
data class QuotationItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteId: Long,
    val itemId: Long,
    val itemName: String,
    val dailyPrice: Double,
    val quantity: Int,
    val totalItemPrice: Double
)
