package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.ui.theme.AccentGold
import com.example.ui.viewmodel.MainViewModel
import com.example.util.DateUtils
import com.example.util.Localization
import com.example.util.StringKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: MainViewModel,
    settings: AppSettings,
    onBillCreated: (Long) -> Unit,
    onQuotationCreated: (Long) -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    val customerNic by viewModel.customerNic.collectAsState()
    val startDateMs by viewModel.startDateMs.collectAsState()
    val endDateMs by viewModel.endDateMs.collectAsState()
    val discountType by viewModel.discountType.collectAsState()
    val discountValue by viewModel.discountValue.collectAsState()
    val customerSuggestions by viewModel.customerSuggestions.collectAsState()

    val days = DateUtils.calculateDays(startDateMs, endDateMs)
    val dailySubtotal = cartItems.sumOf { it.totalItemPrice }
    val totalSubtotal = dailySubtotal * days

    val discountAmount = when (discountType) {
        "FLAT" -> discountValue
        "PERCENT" -> totalSubtotal * (discountValue / 100.0)
        else -> 0.0
    }

    val totalDeposit = cartItems.sumOf { it.item.depositAmount * it.quantity }
    val grandTotal = (totalSubtotal - discountAmount).coerceAtLeast(0.0) + totalDeposit

    var showDiscountDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Customer Information Card (Predictive CRM)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customer Information (CRM)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Predictive Search Phone Field
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { viewModel.onSearchCustomerPhone(it) },
                        label = { Text(Localization.getString(StringKey.CUSTOMER_PHONE, settings.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                        singleLine = true
                    )

                    // Customer Auto-complete Popup
                    if (customerSuggestions.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            tonalElevation = 4.dp,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column {
                                customerSuggestions.forEach { customer ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectCustomer(customer) }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = customer.phone, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { viewModel.customerName.value = it },
                        label = { Text(Localization.getString(StringKey.CUSTOMER_NAME, settings.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerNic,
                        onValueChange = { viewModel.customerNic.value = it },
                        label = { Text(Localization.getString(StringKey.CUSTOMER_NIC, settings.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null) },
                        singleLine = true
                    )
                }
            }
        }

        // Rental Period Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.getString(StringKey.RENTAL_DATES, settings.language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Start Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = DateUtils.formatDate(startDateMs),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$days Days",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Return Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = DateUtils.formatDate(endDateMs),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Selected Equipment Cart Items
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected Equipment (${cartItems.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearCart() }) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No equipment added to cart.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Go to Inventory tab to add items.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(cartItems) { cartItem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cartItem.item.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${DateUtils.formatCurrency(cartItem.item.dailyPrice)} / day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.updateCartQty(cartItem.item.id, cartItem.quantity - 1) }) {
                                Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                            }
                            Text(
                                text = "${cartItem.quantity}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(onClick = { viewModel.updateCartQty(cartItem.item.id, cartItem.quantity + 1) }) {
                                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Increase")
                            }
                        }
                    }
                }
            }
        }

        // Summary & Calculations
        if (cartItems.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rental Subtotal ($days Days):")
                            Text(DateUtils.formatCurrency(totalSubtotal), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Discount: ")
                                TextButton(onClick = { showDiscountDialog = true }) {
                                    Text(
                                        text = if (discountType == "NONE") "Apply Discount" else "$discountType ($discountValue)",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text("- ${DateUtils.formatCurrency(discountAmount)}", color = Color.Red)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Refundable Security Deposit:")
                            Text(DateUtils.formatCurrency(totalDeposit), fontWeight = FontWeight.Bold)
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "GRAND TOTAL:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = DateUtils.formatCurrency(grandTotal),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // Dual Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.createQuotation(onQuotationCreated) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.RequestQuote, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.getString(StringKey.SAVE_QUOTATION, settings.language))
                    }

                    Button(
                        onClick = { viewModel.createRentalBill(onBillCreated) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.getString(StringKey.CREATE_BILL, settings.language))
                    }
                }
            }
        }
    }

    if (showDiscountDialog) {
        var tempValueStr by remember { mutableStateOf(discountValue.toString()) }
        var tempType by remember { mutableStateOf(discountType) }

        AlertDialog(
            onDismissRequest = { showDiscountDialog = false },
            title = { Text("Custom Discount") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = tempType == "FLAT",
                            onClick = { tempType = "FLAT" },
                            label = { Text("Flat LKR") }
                        )
                        FilterChip(
                            selected = tempType == "PERCENT",
                            onClick = { tempType = "PERCENT" },
                            label = { Text("Percentage %") }
                        )
                    }

                    OutlinedTextField(
                        value = tempValueStr,
                        onValueChange = { tempValueStr = it },
                        label = { Text("Discount Value") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.discountType.value = tempType
                        viewModel.discountValue.value = tempValueStr.toDoubleOrNull() ?: 0.0
                        showDiscountDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
