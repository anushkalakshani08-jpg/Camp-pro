package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CustomerEntity
import com.example.data.model.ItemEntity
import com.example.data.model.QuotationEntity
import com.example.data.model.QuotationItemEntity
import com.example.data.model.RentalBillEntity
import com.example.data.model.RentalItemEntity

@Database(
    entities = [
        ItemEntity::class,
        CustomerEntity::class,
        RentalBillEntity::class,
        RentalItemEntity::class,
        QuotationEntity::class,
        QuotationItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CampRentDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun customerDao(): CustomerDao
    abstract fun rentalDao(): RentalDao
    abstract fun quotationDao(): QuotationDao

    companion object {
        @Volatile
        private var INSTANCE: CampRentDatabase? = null

        fun getDatabase(context: Context): CampRentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CampRentDatabase::class.java,
                    "camprent_database"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
