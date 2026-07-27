package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Tents, Cooking & BBQ, Sleeping & Shelter, Lighting & Power, Hiking Gear, Electronics
    val dailyPrice: Double,
    val stockQuantity: Int,
    val rentedQuantity: Int = 0,
    val depositAmount: Double = 0.0,
    val description: String = "",
    val iconName: String = "ic_tent"
) {
    val availableQuantity: Int
        get() = (stockQuantity - rentedQuantity).coerceAtLeast(0)
}
