package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.CampRentDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.AppPalette
import com.example.data.model.AppSettings
import com.example.data.model.AppThemeMode
import com.example.data.model.CustomerEntity
import com.example.data.model.ItemEntity
import com.example.data.model.QuotationEntity
import com.example.data.model.QuotationItemEntity
import com.example.data.model.RentalBillEntity
import com.example.data.model.RentalItemEntity
import com.example.data.model.RentalStatus
import com.example.data.model.repository.CampRentRepository
import com.example.data.preferences.UserPreferencesRepository
import com.example.notification.NotificationHelper
import com.example.util.DateUtils
import com.example.util.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class CartItem(
    val item: ItemEntity,
    var quantity: Int = 1
) {
    val totalItemPrice: Double
        get() = item.dailyPrice * quantity
}

data class DashboardStats(
    val activeRentalsCount: Int,
    val dueTodayCount: Int,
    val overdueCount: Int,
    val totalRevenue: Double,
    val availableStockCount: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CampRentDatabase.getDatabase(application)
    private val repository = CampRentRepository(
        database.itemDao(),
        database.customerDao(),
        database.rentalDao(),
        database.quotationDao()
    )
    private val preferencesRepository = UserPreferencesRepository(application)

    val settings: StateFlow<AppSettings> = preferencesRepository.settings

    val allItems: StateFlow<List<ItemEntity>> = repository.allItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBills: StateFlow<List<RentalBillEntity>> = repository.allBills.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allQuotations: StateFlow<List<QuotationEntity>> = repository.allQuotations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dashboard Statistics Flow
    val dashboardStats: StateFlow<DashboardStats> = combine(
        allBills,
        allItems
    ) { bills, items ->
        val startOfDay = DateUtils.getTodayStartMs()
        val endOfDay = DateUtils.getTodayEndMs()

        val active = bills.filter { it.status == RentalStatus.ACTIVE }
        val dueToday = active.filter { it.endDate in startOfDay..endOfDay }
        val overdue = active.filter { it.endDate < startOfDay }
        val revenue = bills.filter { it.status != RentalStatus.CANCELLED }.sumOf { it.grandTotal - it.totalDeposit }
        val availStock = items.sumOf { it.availableQuantity }

        DashboardStats(
            activeRentalsCount = active.size,
            dueTodayCount = dueToday.size,
            overdueCount = overdue.size,
            totalRevenue = revenue,
            availableStockCount = availStock
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats(0, 0, 0, 0.0, 0)
    )

    // New Bill / Quotation Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Customer Form State
    var customerName = MutableStateFlow("")
    var customerPhone = MutableStateFlow("")
    var customerNic = MutableStateFlow("")

    // Rental Period State
    val startDateMs = MutableStateFlow(System.currentTimeMillis())
    val endDateMs = MutableStateFlow(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L)) // 3 days default

    // Discount & Deposit State
    val discountType = MutableStateFlow("NONE") // NONE, FLAT, PERCENT
    val discountValue = MutableStateFlow(0.0)

    val customerSuggestions = MutableStateFlow<List<CustomerEntity>>(emptyList())

    val userMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
            NotificationHelper.scheduleDailyReturnCheck(application)
        }
    }

    fun onSearchCustomerPhone(query: String) {
        customerPhone.value = query
        if (query.length >= 3) {
            viewModelScope.launch {
                customerSuggestions.value = repository.searchCustomers(query)
            }
        } else {
            customerSuggestions.value = emptyList()
        }
    }

    fun selectCustomer(customer: CustomerEntity) {
        customerName.value = customer.name
        customerPhone.value = customer.phone
        customerNic.value = customer.nicOrPassport
        customerSuggestions.value = emptyList()
    }

    fun addToCart(item: ItemEntity, qty: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.item.id == item.id }
        if (index >= 0) {
            val existing = current[index]
            val newQty = existing.quantity + qty
            if (newQty <= item.availableQuantity) {
                current[index] = existing.copy(quantity = newQty)
            } else {
                userMessage.value = "Selected quantity exceeds available stock!"
            }
        } else {
            if (qty <= item.availableQuantity) {
                current.add(CartItem(item = item, quantity = qty))
            } else {
                userMessage.value = "Selected quantity exceeds available stock!"
            }
        }
        _cartItems.value = current
    }

    fun updateCartQty(itemId: Long, qty: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.item.id == itemId }
        if (index >= 0) {
            if (qty <= 0) {
                current.removeAt(index)
            } else {
                val item = current[index].item
                if (qty <= item.availableQuantity) {
                    current[index] = current[index].copy(quantity = qty)
                } else {
                    userMessage.value = "Selected quantity exceeds available stock!"
                }
            }
        }
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        customerName.value = ""
        customerPhone.value = ""
        customerNic.value = ""
        discountValue.value = 0.0
        discountType.value = "NONE"
    }

    fun createRentalBill(onSuccess: (Long) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            userMessage.value = "Cart is empty!"
            return
        }
        if (customerName.value.isBlank() || customerPhone.value.isBlank()) {
            userMessage.value = "Please enter Customer Name and Phone Number!"
            return
        }

        val days = DateUtils.calculateDays(startDateMs.value, endDateMs.value)
        val dailySubtotal = items.sumOf { it.totalItemPrice }
        val subtotal = dailySubtotal * days

        val discVal = discountValue.value
        val discountAmount = when (discountType.value) {
            "FLAT" -> discVal
            "PERCENT" -> subtotal * (discVal / 100.0)
            else -> 0.0
        }

        val totalDeposit = items.sumOf { it.item.depositAmount * it.quantity }
        val grandTotal = (subtotal - discountAmount).coerceAtLeast(0.0) + totalDeposit

        val bill = RentalBillEntity(
            billNumber = DateUtils.generateBillNumber(),
            customerName = customerName.value.trim(),
            customerPhone = customerPhone.value.trim(),
            customerNic = customerNic.value.trim(),
            startDate = startDateMs.value,
            endDate = endDateMs.value,
            totalDays = days,
            subtotal = subtotal,
            discountType = discountType.value,
            discountValue = discVal,
            totalDeposit = totalDeposit,
            grandTotal = grandTotal,
            status = RentalStatus.ACTIVE
        )

        val rentalItems = items.map {
            RentalItemEntity(
                billId = 0,
                itemId = it.item.id,
                itemName = it.item.name,
                itemCategory = it.item.category,
                dailyPrice = it.item.dailyPrice,
                quantity = it.quantity,
                totalItemPrice = it.totalItemPrice * days
            )
        }

        viewModelScope.launch {
            val billId = repository.createRentalBill(bill, rentalItems)
            clearCart()
            userMessage.value = "Rental Bill #${bill.billNumber} created successfully!"
            onSuccess(billId)
        }
    }

    fun createQuotation(onSuccess: (Long) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            userMessage.value = "Cart is empty!"
            return
        }
        if (customerName.value.isBlank() || customerPhone.value.isBlank()) {
            userMessage.value = "Please enter Customer Name and Phone Number!"
            return
        }

        val days = DateUtils.calculateDays(startDateMs.value, endDateMs.value)
        val dailySubtotal = items.sumOf { it.totalItemPrice }
        val subtotal = dailySubtotal * days

        val discVal = discountValue.value
        val discountAmount = when (discountType.value) {
            "FLAT" -> discVal
            "PERCENT" -> subtotal * (discVal / 100.0)
            else -> 0.0
        }

        val totalDeposit = items.sumOf { it.item.depositAmount * it.quantity }
        val grandTotal = (subtotal - discountAmount).coerceAtLeast(0.0) + totalDeposit

        val quote = QuotationEntity(
            quoteNumber = DateUtils.generateQuoteNumber(),
            customerName = customerName.value.trim(),
            customerPhone = customerPhone.value.trim(),
            customerNic = customerNic.value.trim(),
            startDate = startDateMs.value,
            endDate = endDateMs.value,
            totalDays = days,
            subtotal = subtotal,
            discountType = discountType.value,
            discountValue = discVal,
            totalDeposit = totalDeposit,
            grandTotal = grandTotal
        )

        val quotationItems = items.map {
            QuotationItemEntity(
                quoteId = 0,
                itemId = it.item.id,
                itemName = it.item.name,
                dailyPrice = it.item.dailyPrice,
                quantity = it.quantity,
                totalItemPrice = it.totalItemPrice * days
            )
        }

        viewModelScope.launch {
            val quoteId = repository.createQuotation(quote, quotationItems)
            clearCart()
            userMessage.value = "Quotation #${quote.quoteNumber} created!"
            onSuccess(quoteId)
        }
    }

    fun convertQuotationToBill(quoteId: Long, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val result = repository.convertQuotationToBill(quoteId)
            result.onSuccess { billId ->
                userMessage.value = "Quotation converted to active Bill!"
                onSuccess(billId)
            }.onFailure { err ->
                userMessage.value = err.message ?: "Failed to convert quotation"
            }
        }
    }

    fun markBillReturned(billId: Long) {
        viewModelScope.launch {
            repository.updateBillStatus(billId, RentalStatus.RETURNED)
            userMessage.value = "Rental marked as returned and stock updated!"
        }
    }

    fun deleteBill(bill: RentalBillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
            userMessage.value = "Bill deleted."
        }
    }

    fun deleteQuotation(quote: QuotationEntity) {
        viewModelScope.launch {
            repository.deleteQuotation(quote)
            userMessage.value = "Quotation deleted."
        }
    }

    // Inventory Item CRUD
    fun saveItem(item: ItemEntity) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertItem(item)
                userMessage.value = "Item added to inventory!"
            } else {
                repository.updateItem(item)
                userMessage.value = "Item updated!"
            }
        }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
            userMessage.value = "Item deleted from inventory."
        }
    }

    // Customer CRM CRUD
    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.insertCustomer(customer)
                userMessage.value = "Customer added!"
            } else {
                repository.updateCustomer(customer)
                userMessage.value = "Customer details updated!"
            }
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            userMessage.value = "Customer removed."
        }
    }

    // Settings
    fun updateThemeMode(mode: AppThemeMode) = preferencesRepository.updateThemeMode(mode)
    fun updatePalette(palette: AppPalette) = preferencesRepository.updatePalette(palette)
    fun updateLanguage(language: AppLanguage) = preferencesRepository.updateLanguage(language)

    fun updateBusinessDetails(
        name: String,
        address: String,
        phone: String,
        email: String,
        terms: String,
        logoPath: String?
    ) {
        preferencesRepository.updateBusinessDetails(name, address, phone, email, terms, logoPath)
        userMessage.value = "Branding details saved!"
    }

    // PDF Generation
    fun generateBillPdfAndShare(context: Context, bill: RentalBillEntity, onResult: (File) -> Unit) {
        viewModelScope.launch {
            val items = repository.getItemsForBill(bill.id)
            val file = PdfGenerator.generateBillPdf(context, bill, items, settings.value)
            onResult(file)
        }
    }

    fun generateQuotationPdfAndShare(context: Context, quote: QuotationEntity, onResult: (File) -> Unit) {
        viewModelScope.launch {
            val items = repository.getItemsForQuotation(quote.id)
            val file = PdfGenerator.generateQuotationPdf(context, quote, items, settings.value)
            onResult(file)
        }
    }

    fun clearUserMessage() {
        userMessage.value = null
    }
}
