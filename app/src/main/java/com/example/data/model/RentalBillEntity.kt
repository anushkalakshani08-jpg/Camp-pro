package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RentalStatus {
    ACTIVE,
    RETURNED,
    OVERDUE,
    CANCELLED
}

@Entity(tableName = "rental_bills")
data class RentalBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billNumber: String,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String,
    val customerNic: String = "",
    val startDate: Long, // timestamp ms
    val endDate: Long,   // timestamp ms
    val totalDays: Int,
    val subtotal: Double,
    val discountType: String = "NONE", // NONE, FLAT, PERCENT
    val discountValue: Double = 0.0,
    val totalDeposit: Double,
    val grandTotal: Double,
    val status: RentalStatus = RentalStatus.ACTIVE,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "rental_items")
data class RentalItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val itemId: Long,
    val itemName: String,
    val itemCategory: String,
    val dailyPrice: Double,
    val quantity: Int,
    val totalItemPrice: Double
)
