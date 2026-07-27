package com.example.data.model.repository

import com.example.data.db.CustomerDao
import com.example.data.db.ItemDao
import com.example.data.db.QuotationDao
import com.example.data.db.RentalDao
import com.example.data.model.CustomerEntity
import com.example.data.model.ItemEntity
import com.example.data.model.QuotationEntity
import com.example.data.model.QuotationItemEntity
import com.example.data.model.RentalBillEntity
import com.example.data.model.RentalItemEntity
import com.example.data.model.RentalStatus
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CampRentRepository(
    private val itemDao: ItemDao,
    private val customerDao: CustomerDao,
    private val rentalDao: RentalDao,
    private val quotationDao: QuotationDao
) {
    val allItems: Flow<List<ItemEntity>> = itemDao.getAllItems()
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val allBills: Flow<List<RentalBillEntity>> = rentalDao.getAllBills()
    val allQuotations: Flow<List<QuotationEntity>> = quotationDao.getAllQuotations()

    suspend fun seedSampleDataIfEmpty() {
        val existingItems = itemDao.getAllItems().first()
        if (existingItems.isEmpty()) {
            val sampleItems = listOf(
                ItemEntity(name = "4-Person Waterproof Dome Tent", category = "Tents & Shelters", dailyPrice = 2500.0, stockQuantity = 8, depositAmount = 5000.0, description = "Double-layer windproof camping tent with rainfly."),
                ItemEntity(name = "2-Person Lightweight Backpacking Tent", category = "Tents & Shelters", dailyPrice = 1800.0, stockQuantity = 12, depositAmount = 3500.0, description = "Compact 2kg tent ideal for hiking and trekking."),
                ItemEntity(name = "6-Person Family Cabin Tent", category = "Tents & Shelters", dailyPrice = 4500.0, stockQuantity = 5, depositAmount = 10000.0, description = "Spacious standing height tent with room dividers."),
                ItemEntity(name = "Portable Stainless Steel BBQ Grill", category = "Cooking & BBQ", dailyPrice = 1200.0, stockQuantity = 10, depositAmount = 2500.0, description = "Foldable charcoal grill with skewers and tongs."),
                ItemEntity(name = "Dual Burner Camping Gas Stove", category = "Cooking & BBQ", dailyPrice = 1500.0, stockQuantity = 7, depositAmount = 3000.0, description = "High power butane stove with carry case."),
                ItemEntity(name = "Non-Stick Outdoor Cookware Set (4-pax)", category = "Cooking & BBQ", dailyPrice = 800.0, stockQuantity = 15, depositAmount = 1500.0, description = "Pots, frying pan, kettle, bowls and spatulas."),
                ItemEntity(name = "-5°C Thermal Sleeping Bag", category = "Sleeping & Mats", dailyPrice = 900.0, stockQuantity = 20, depositAmount = 2000.0, description = "Warm synthetic insulation sleeping bag."),
                ItemEntity(name = "Self-Inflating Foam Sleeping Pad", category = "Sleeping & Mats", dailyPrice = 500.0, stockQuantity = 25, depositAmount = 1000.0, description = "5cm thick comfortable moisture-proof mattress."),
                ItemEntity(name = "1000 Lumen Rechargeable LED Lantern", category = "Lighting & Power", dailyPrice = 600.0, stockQuantity = 18, depositAmount = 1200.0, description = "Dimmable camping light with power bank output."),
                ItemEntity(name = "300W Portable Solar Power Station", category = "Lighting & Power", dailyPrice = 3500.0, stockQuantity = 4, depositAmount = 15000.0, description = "AC/DC/USB output power bank for phones and laptops."),
                ItemEntity(name = "60L Ergonomic Trekking Backpack", category = "Hiking Gear", dailyPrice = 1000.0, stockQuantity = 10, depositAmount = 3000.0, description = "Adjustable back length with integrated rain cover."),
                ItemEntity(name = "Aluminum Trekking Poles (Pair)", category = "Hiking Gear", dailyPrice = 400.0, stockQuantity = 15, depositAmount = 1000.0, description = "Shock absorbing collapsible hiking sticks.")
            )

            sampleItems.forEach { itemDao.insertItem(it) }

            // Sample Customers
            val c1 = CustomerEntity(name = "Kasun Perera", phone = "0771234567", nicOrPassport = "199234512340", email = "kasun@gmail.com", address = "Colombo 07", totalRentalsCount = 2)
            val c2 = CustomerEntity(name = "Nimal Fernando", phone = "0718889900", nicOrPassport = "198812398765", email = "nimal@yahoo.com", address = "Kandy City", totalRentalsCount = 1)
            val c3 = CustomerEntity(name = "Samanthi Silva", phone = "0754443322", nicOrPassport = "199577889900", email = "samanthi@gmail.com", address = "Galle Road, Panadura", totalRentalsCount = 0)

            val cid1 = customerDao.insertCustomer(c1)
            val cid2 = customerDao.insertCustomer(c2)
            customerDao.insertCustomer(c3)

            // Sample Active Rental Bill
            val now = System.currentTimeMillis()
            val threeDaysLater = now + (3 * 24 * 60 * 60 * 1000L)
            val sampleBill = RentalBillEntity(
                billNumber = DateUtils.generateBillNumber(),
                customerId = cid1,
                customerName = "Kasun Perera",
                customerPhone = "0771234567",
                customerNic = "199234512340",
                startDate = now,
                endDate = threeDaysLater,
                totalDays = 3,
                subtotal = 11700.0,
                discountType = "FLAT",
                discountValue = 700.0,
                totalDeposit = 10000.0,
                grandTotal = 21000.0, // (11700 - 700) + 10000 deposit
                status = RentalStatus.ACTIVE,
                notes = "Ohiya Knuckles trekking trip"
            )

            val billId = rentalDao.insertBill(sampleBill)
            val rentalItems = listOf(
                RentalItemEntity(billId = billId, itemId = 1, itemName = "4-Person Waterproof Dome Tent", itemCategory = "Tents & Shelters", dailyPrice = 2500.0, quantity = 1, totalItemPrice = 7500.0),
                RentalItemEntity(billId = billId, itemId = 4, itemName = "Portable Stainless Steel BBQ Grill", itemCategory = "Cooking & BBQ", dailyPrice = 1200.0, quantity = 1, totalItemPrice = 3600.0),
                RentalItemEntity(billId = billId, itemId = 9, itemName = "1000 Lumen Rechargeable LED Lantern", itemCategory = "Lighting & Power", dailyPrice = 600.0, quantity = 1, totalItemPrice = 1800.0)
            )
            rentalDao.insertRentalItems(rentalItems)
            // Update stock
            itemDao.updateRentedQuantity(1, 1)
            itemDao.updateRentedQuantity(4, 1)
            itemDao.updateRentedQuantity(9, 1)

            // Sample Quotation
            val sampleQuote = QuotationEntity(
                quoteNumber = DateUtils.generateQuoteNumber(),
                customerName = "Nimal Fernando",
                customerPhone = "0718889900",
                customerNic = "198812398765",
                startDate = now + (7 * 24 * 60 * 60 * 1000L),
                endDate = now + (9 * 24 * 60 * 60 * 1000L),
                totalDays = 2,
                subtotal = 9000.0,
                discountType = "NONE",
                discountValue = 0.0,
                totalDeposit = 10000.0,
                grandTotal = 19000.0,
                notes = "Estimate for Sinharaja photography expedition"
            )
            val quoteId = quotationDao.insertQuotation(sampleQuote)
            quotationDao.insertQuotationItems(listOf(
                QuotationItemEntity(quoteId = quoteId, itemId = 3, itemName = "6-Person Family Cabin Tent", dailyPrice = 4500.0, quantity = 1, totalItemPrice = 9000.0)
            ))
        }
    }

    // Item Operations
    suspend fun insertItem(item: ItemEntity): Long = itemDao.insertItem(item)
    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(item)
    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)

    // Customer Operations
    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)
    suspend fun searchCustomers(query: String): List<CustomerEntity> = customerDao.searchCustomers("%$query%")

    // Rental Bill Operations
    suspend fun getBillById(id: Long): RentalBillEntity? = rentalDao.getBillById(id)
    suspend fun getItemsForBill(billId: Long): List<RentalItemEntity> = rentalDao.getItemsForBill(billId)

    suspend fun createRentalBill(
        bill: RentalBillEntity,
        items: List<RentalItemEntity>
    ): Long {
        val billId = rentalDao.insertBill(bill)
        val itemsWithBillId = items.map { it.copy(billId = billId) }
        rentalDao.insertRentalItems(itemsWithBillId)

        // Increment stock rentedQuantity for active items
        if (bill.status == RentalStatus.ACTIVE) {
            items.forEach {
                itemDao.updateRentedQuantity(it.itemId, it.quantity)
            }
        }

        // Auto save / update customer
        if (bill.customerPhone.isNotBlank()) {
            val existing = customerDao.searchCustomers("%${bill.customerPhone}%").firstOrNull()
            if (existing != null) {
                customerDao.incrementRentalCount(existing.id)
            } else {
                customerDao.insertCustomer(
                    CustomerEntity(
                        name = bill.customerName,
                        phone = bill.customerPhone,
                        nicOrPassport = bill.customerNic,
                        totalRentalsCount = 1
                    )
                )
            }
        }

        return billId
    }

    suspend fun updateBillStatus(billId: Long, newStatus: RentalStatus) {
        val bill = rentalDao.getBillById(billId) ?: return
        val items = rentalDao.getItemsForBill(billId)

        if (bill.status == RentalStatus.ACTIVE && newStatus == RentalStatus.RETURNED) {
            // Release rented stock back to inventory!
            items.forEach {
                itemDao.updateRentedQuantity(it.itemId, -it.quantity)
            }
        }

        rentalDao.updateBill(bill.copy(status = newStatus))
    }

    suspend fun deleteBill(bill: RentalBillEntity) {
        if (bill.status == RentalStatus.ACTIVE) {
            val items = rentalDao.getItemsForBill(bill.id)
            items.forEach {
                itemDao.updateRentedQuantity(it.itemId, -it.quantity)
            }
        }
        rentalDao.deleteItemsForBill(bill.id)
        rentalDao.deleteBill(bill)
    }

    // Quotation Operations
    suspend fun createQuotation(
        quote: QuotationEntity,
        items: List<QuotationItemEntity>
    ): Long {
        val quoteId = quotationDao.insertQuotation(quote)
        val itemsWithQuoteId = items.map { it.copy(quoteId = quoteId) }
        quotationDao.insertQuotationItems(itemsWithQuoteId)
        return quoteId
    }

    suspend fun getItemsForQuotation(quoteId: Long): List<QuotationItemEntity> = quotationDao.getItemsForQuotation(quoteId)

    suspend fun convertQuotationToBill(quoteId: Long): Result<Long> {
        val quote = quotationDao.getQuotationById(quoteId) ?: return Result.failure(Exception("Quotation not found"))
        val quoteItems = quotationDao.getItemsForQuotation(quoteId)

        // Verify stock availability
        for (qi in quoteItems) {
            val item = itemDao.getItemById(qi.itemId)
            if (item == null || item.availableQuantity < qi.quantity) {
                return Result.failure(Exception("Insufficient stock available for '${qi.itemName}'"))
            }
        }

        // Create Active Rental Bill
        val bill = RentalBillEntity(
            billNumber = DateUtils.generateBillNumber(),
            customerName = quote.customerName,
            customerPhone = quote.customerPhone,
            customerNic = quote.customerNic,
            startDate = quote.startDate,
            endDate = quote.endDate,
            totalDays = quote.totalDays,
            subtotal = quote.subtotal,
            discountType = quote.discountType,
            discountValue = quote.discountValue,
            totalDeposit = quote.totalDeposit,
            grandTotal = quote.grandTotal,
            status = RentalStatus.ACTIVE,
            notes = "Converted from Quote #${quote.quoteNumber}"
        )

        val rentalItems = quoteItems.map {
            RentalItemEntity(
                billId = 0,
                itemId = it.itemId,
                itemName = it.itemName,
                itemCategory = "Equipment",
                dailyPrice = it.dailyPrice,
                quantity = it.quantity,
                totalItemPrice = it.totalItemPrice
            )
        }

        val billId = createRentalBill(bill, rentalItems)

        // Mark quote as converted
        quotationDao.updateQuotation(quote.copy(isConvertedToBill = true, convertedBillId = billId))

        return Result.success(billId)
    }

    suspend fun deleteQuotation(quote: QuotationEntity) {
        quotationDao.deleteItemsForQuotation(quote.id)
        quotationDao.deleteQuotation(quote)
    }
}
