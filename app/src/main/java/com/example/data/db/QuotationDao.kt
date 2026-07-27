package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.QuotationEntity
import com.example.data.model.QuotationItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotationDao {
    @Query("SELECT * FROM quotations ORDER BY createdAt DESC")
    fun getAllQuotations(): Flow<List<QuotationEntity>>

    @Query("SELECT * FROM quotations WHERE id = :id")
    suspend fun getQuotationById(id: Long): QuotationEntity?

    @Query("SELECT * FROM quotation_items WHERE quoteId = :quoteId")
    suspend fun getItemsForQuotation(quoteId: Long): List<QuotationItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: QuotationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotationItems(items: List<QuotationItemEntity>)

    @Update
    suspend fun updateQuotation(quotation: QuotationEntity)

    @Delete
    suspend fun deleteQuotation(quotation: QuotationEntity)

    @Query("DELETE FROM quotation_items WHERE quoteId = :quoteId")
    suspend fun deleteItemsForQuotation(quoteId: Long)
}
