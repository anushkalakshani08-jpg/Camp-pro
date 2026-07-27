package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val nicOrPassport: String = "",
    val email: String = "",
    val address: String = "",
    val totalRentalsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
