package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.RentalBillEntity
import com.example.data.model.RentalItemEntity
import com.example.data.model.RentalStatus
import kotlinx.coroutines.flow.Flow

data class RentalBillWithItems(
    val bill: RentalBillEntity,
    val items: List<RentalItemEntity>
)

@Dao
interface RentalDao {
    @Query("SELECT * FROM rental_bills ORDER BY createdAt DESC")
    fun getAllBills(): Flow<List<RentalBillEntity>>

    @Query("SELECT * FROM rental_bills WHERE status = :status ORDER BY startDate ASC")
    fun getBillsByStatus(status: RentalStatus): Flow<List<RentalBillEntity>>

    @Query("SELECT * FROM rental_bills WHERE id = :id")
    suspend fun getBillById(id: Long): RentalBillEntity?

    @Query("SELECT * FROM rental_items WHERE billId = :billId")
    suspend fun getItemsForBill(billId: Long): List<RentalItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: RentalBillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRentalItems(items: List<RentalItemEntity>)

    @Update
    suspend fun updateBill(bill: RentalBillEntity)

    @Delete
    suspend fun deleteBill(bill: RentalBillEntity)

    @Query("DELETE FROM rental_items WHERE billId = :billId")
    suspend fun deleteItemsForBill(billId: Long)

    @Query("SELECT * FROM rental_bills WHERE endDate >= :startOfDay AND endDate <= :endOfDay AND status = 'ACTIVE'")
    suspend fun getRentalsDueOnDate(startOfDay: Long, endOfDay: Long): List<RentalBillEntity>
}
